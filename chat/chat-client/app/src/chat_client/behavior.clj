; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns ^:shared chat-client.behavior
  (:require [clojure.set :as set]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.platform :as platform]
            [io.pedestal.app.util.log :as log]
            [chat-client.util :as util]))

;; Transforms

(defn outbound-transform [state message]
  (case (msg/type message)
    msg/init (:value message)
    :send-message (let [msg {:id (util/random-id)
                             :time (platform/date)
                             :nickname (:nickname message)
                             :text (:text message)
                             :status :pending}]
                    (-> state
                        (update-in [:sent] conj msg)
                        (assoc :sending msg)))
    :clear-messages (do (log/debug :in :clear-messages :transform :outbound) (-> state
                               (assoc :sent [])
                               (dissoc :sending)))
    state))

(defn inbound-transform [state message]
  (case (msg/type message)
    msg/init (:value message)
    :received (let [msg {:id (:id message) :time (platform/date)
                         :nickname (:nickname message) :text (:text message)}]
                (update-in state [:received] conj msg))
    :clear-messages (do (log/debug :in :clear-messages :transform :inbound) ( assoc state :received []))))

(defn nickname-transform [state message]
  (case (msg/type message)
    msg/init (:value message)
    :set-nickname (:nickname message)
    :clear-nickname nil))

;; Combines

(defn diff-by [f new old]
  (let [o (set (map f old))
        n (set (map f new))
        new-keys (set/difference n o)]
    (filter (comp new-keys f) new)))

(defn new-msgs [{:keys [old new]} k]
  (let [o (set (k old))
        n (set (k new))]
    (diff-by :id n o)))

(defn deleted-msgs [{:keys [old new]} k]
  (log/debug :in :deleted-msgs :old old :new new)
  (let [o (set (k old))
        n (set (k new))]
    (diff-by :id o n)))

(defn new-messages [state inputs]
  (let [in (new-msgs (:inbound inputs) :received)]
    (sort-by :time in)))

(defn deleted-messages [state inputs]
  (let [in (deleted-msgs (:inbound inputs) :received)
        out (deleted-msgs (:outbound inputs) :sent)]
    (concat in out)))

(defn- index-by [coll key]
  (reduce
   (fn [a x]
     (assoc a (key x) x))
   {}
   coll))
 
(defn- updated-message? [reference-messages msg]
  (when-let [reference-msg (reference-messages (:id msg))]
    (not= (:time reference-msg) (:time msg))))

(defn updated-messages [state inputs]
  (let [new-msgs [:new-messages :new]
        out-msgs-index (index-by (get-in inputs [:outbound :new :sent]) :id)]
    (let [updated-msgs  (filter (partial updated-message? out-msgs-index) new-msgs)]
      (map #(assoc % :status :confirmed) updated-msgs))))

;; Effect

(defn send-message-to-server [_ _ outbound]
  [{msg/topic :server :out-message (:sending outbound)}])


;; Emits

(def ^:private initial-app-model
  [{:chat
    {:log {}
     :form
     {:transforms
      {:clear-messages [{msg/topic :outbound} {msg/topic :inbound}]
       :set-nickname [{msg/topic :nickname (msg/param :nickname) {}}]}}}}])

(defn- new-deltas [value]
  (vec (mapcat (fn [{:keys [id] :as msg}]
                 [[:node-create [:chat :log id] :map]
                  [:value [:chat :log id] msg]])
               value)))

(defn- delete-deltas [value]
  (vec (mapcat (fn [{:keys [id] :as msg}]
                 [[:node-destroy [:chat :log id]]])
               value)))

(defn- update-deltas [value]
  (mapv (fn [{:keys [id] :as msg}]
          [:value [:chat :log id] msg]) value))

(defn- nickname-deltas [nickname]
  (if nickname
    [[:node-create [:chat :nickname] :map]
     [:value [:chat :nickname] nickname]
     [:transform-enable [:chat :form] :clear-nickname [{msg/topic :nickname}]]
     [:transform-enable [:chat :form] :send-message [{msg/topic :outbound
                                                      (msg/param :text) {}
                                                      :nickname nickname}]]
     [:transform-disable [:chat :form] :set-nickname]]
    
    [[:node-destroy [:chat :nickname]]
     [:transform-disable [:chat :form] :clear-nickname]
     [:transform-disable [:chat :form] :send-message]
     [:transform-enable [:chat :form] :set-nickname [{msg/topic :nickname
                                                      (msg/param :nickname) {}}]]]))

(def sort-order
  {:new-messages 0
   :deleted-messages 1
   :updated-messages 2})

(defn chat-emit
  ([inputs] initial-app-model)
  ([inputs changed-inputs]
     (reduce (fn [a input-name]
               (let [new-value (:new (get inputs input-name))]
                 (concat a (case input-name
                             :new-messages (new-deltas new-value)
                             :deleted-messages (delete-deltas new-value)
                             :updated-messages (update-deltas new-value)
                             :nickname (nickname-deltas new-value)
                             []))))
             []
             (sort-by sort-order changed-inputs))))

;; Dataflow

(def chat-client
  {:transform {:outbound {:init {} :fn outbound-transform}
               :inbound  {:init {} :fn inbound-transform}
               :nickname {:init nil :fn nickname-transform}}
   :effect  {:outbound send-message-to-server}
   :combine {:new-messages      {:fn new-messages     :input #{:inbound :outbound}}
             :updated-messages  {:fn updated-messages :input #{:outbound :new-messages}}
             :deleted-messages  {:fn deleted-messages :input #{:inbound :outbound}}}
   :emit  {:emit {:fn chat-emit :input #{:new-messages :deleted-messages :updated-messages :nickname}}}})
