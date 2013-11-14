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
        (update-in [:sent] conj msg)
        (assoc :sending msg))))

(defn send-message [[[_ _ value]] _]
  [[[[:info] add-message value]]])

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
        [(inspect "<<<<<<<<") [:**] :*]]

   :out [[(inspect ">>>>>>>>") [:**] :*]]})
