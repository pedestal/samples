(ns ^:shared chat-client.behavior
    (:require [clojure.string :as string]
              [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]))

(defn nickname-transform
  [_ message]
  (:nickname message))

(defn init-app-model [_]
  [{:chat
    {:log {}
     :form
     {:transforms
      {:clear-messages [{msg/topic [:outbound]} {msg/topic [:inbound]}]
       :set-nickname [{msg/topic [:nickname] (msg/param :nickname) {}}]}}}}])

(def example-app
  {:version 2
   :transform [
               [:set-nickname [:nickname] nickname-transform]]
   :emit [{:init init-app-model}
          [#{[:*]} (app/default-emitter nil)]]})

