(ns ^:shared chat-client.app
  (:require [chat-client.util :as util]
            [io.pedestal.app.util.platform :as platform]))

(defn visible-widgets [[[_ event wid]]]
  (cond (= event :created-widget) [[[[:info :visible] (fnil conj #{}) wid]]]
        (= event :removed-widget) [[[[:info :visible] disj wid]]]))

(defn startup [inform-message]
  [[[[:ui :root] :change-screen :chat [:ui :chat]]]])

(defn set-nickname [[[_ _ value]]]
  [[[[:info :nickname] (constantly (:nickname value))]
    [[:ui :chat] :nickname-set]
    ;; TODO - display nickname
    ]])

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
  [info message]
  (let [msg {:id (:id message)
             :time (platform/date)
             :nickname (:nickname message)
             :text (:text message)}]
    (update-in info [:received] conj msg)))

(defn inbound-received [[[_ _ msg]]]
  [[[[:info :inbound] inbound-received* msg]]])

(defn send-outbound [[[_ _ _ model]]]
  ;; Can't use path since it's actually triggered at [:info :outbound :sending :*]
  [[[[:services :message] :receive-inbound (get-in model [:info :outbound :sending])]]])

(defn inspect [s]
  (fn [inform-message]
    (.log js/console s)
    (.log js/console (pr-str inform-message))
    []))

(def config
  {:in [[visible-widgets [:registry] :*]
        [startup [:app] :startup]
        [set-nickname [:ui :set-nickname] :click]
        [clear-nickname [:ui :clear-nickname] :click]
        [send-message [:ui :send-message] :click]
        [inbound-received [:services :message] :inbound-received]
        [(inspect "<<<<<<<<") [:**] :*]]

   :out [[send-outbound [:info :outbound :sending :*] :*]
         [(inspect ">>>>>>>>") [:**] :*]]})
