(ns tutorial-client.simulated.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.platform :as platform]))

(def counters (atom {"abc" 10
                     "xyz" 0}))

(defn receive-messages [app]
  (p/put-message (:input app) {msg/type :set-value msg/topic [:other-counters "abc"]
                               :value (get (swap! counters update-in ["abc"] inc)
                                           "abc")})
  (p/put-message (:input app) {msg/type :set-value msg/topic [:other-counters "xyz"]
                               :value (get (swap! counters update-in ["xyz"] inc)
                                           "xyz")})
  (platform/create-timeout 1000 #(receive-messages app)))

(defrecord MockServices [app]
  p/Activity
  (start [this]
    (receive-messages app))
  (stop [this]))

(defn services-fn [message input-queue]
  (.log js/console (str "Sending message to server: " message)))
