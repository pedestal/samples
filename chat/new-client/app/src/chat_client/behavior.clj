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

(defn chat-emit
  [inputs]
  (prn "EMIT" inputs)
  ((app/default-emitter nil) inputs))

(defn chat-emit2
  [inputs]
  (prn "EMIT2" inputs)
  [])

(def example-app
  {:version 2
   :transform [
               [:set-nickname [:nickname] nickname-transform]]
   :emit [{:init init-app-model}
          ;[#{[:*]} chat-emit2]
          [#{[:*]} chat-emit]
          ;[#{[:*]} (app/default-emitter nil)]
          ]})

