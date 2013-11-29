(ns ^:shared chat-client.app
  (:require [chat-client.util :as util]
            [clojure.set :as set]
            [io.pedestal.app.util.platform :as platform]))

(defn visible-widgets [[[_ event wid]]]
  (cond (= event :created-widget) [[[[:info :visible] (fnil conj #{}) wid]]]
        (= event :removed-widget) [[[[:info :visible] disj wid]]]))

(defn startup [inform-message]
  [[[[:ui :root] :change-screen :chat [:ui :chat]]
    [[:services :message] :start-receiving]]])

(defn set-nickname [[[_ _ value]]]
  [[[[:info :nickname] (constantly (:nickname value))]
    [[:ui :chat] :nickname-set (:nickname value)]]])

(defn clear-nickname [[[_ _ value]]]
  [[[[:ui :chat] :nickname-cleared]
    [[:info] dissoc :nickname]]])

(defn add-message [info message]
  (let [msg {:id (util/random-id)
             :time (platform/date)
             :nickname (:nickname info)
             :text (:text message)
             :status :pending}]
    (-> info
        (update-in [:outbound :sent] conj msg)
        (assoc-in [:outbound :sending] msg))))

(defn send-message [[[_ _ msg]]]
  [[[[:info] add-message msg]]])

(defn inbound-received*
  [current message]
  (let [msg {:id (:id message)
             :time (platform/date)
             :nickname (:nickname message)
             :text (:text message)}]
    (update-in current [:received] conj msg)))

(defn inbound-received [[[_ _ msg]]]
  [[[[:info :inbound] inbound-received* msg]]])

(defn send-outbound [[[_ _ _ model]]]
  ;; Can't use path since it's actually triggered at [:info :outbound :sending :*]
  [[[[:services :message] :receive-inbound (get-in model [:info :outbound :sending])]]])


(defn diff-by [f new old]
  (let [o (set (map f old))
        n (set (map f new))
        new-keys (set/difference n o)]
    (filter (comp new-keys f) new)))

(defn new-msgs [{:keys [old new]} k]
  (let [o (set (get-in old k))
        n (set (get-in new k))]
    (diff-by :id n o)))

(defn add-new-messages [[[path event old new]]]
  [[[[:info :new-messages]
     (constantly (sort-by :time (new-msgs {:old old :new new} path)))]]])

(defn add-logs [[[path _ _ new]]]
  [[[[:ui :chat] :add-logs (get-in new path)]]])

(defn inspect [s]
  (fn [inform-message]
    (.log js/console s)
    (.log js/console (pr-str inform-message))
    []))

;; Info Model Paths
;; [:nickname] - Nickname for chat user
;; [:inbound :received] - Received inbound messages
;; [:outbound :sent] - Sent outbound messages
;; [:outbound :sending] - Pending message that goes to service
;; [:new-messages] - Messages that are new, determined by id
;; [:visible] - set of visible widgets
(def config
  {:in [[visible-widgets [:registry] :*]
        [startup [:app] :startup]
        [set-nickname [:ui :set-nickname] :click]
        [clear-nickname [:ui :clear-nickname] :click]
        [send-message [:ui :send-message] :click]
        [inbound-received [:services :message] :inbound-received]
        [(inspect "<<<<<<<<") [:**] :*]]

   :out [[send-outbound [:info :outbound :sending :*] :*]
         [add-new-messages [:info :inbound :received] :*]
         [add-logs [:info :new-messages] :*]
         [(inspect ">>>>>>>>") [:**] :*]]})
