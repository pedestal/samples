(ns ^:shared tutorial-client.behavior
    (:require [clojure.string :as string]
              [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]
              [io.pedestal.app.dataflow :as dataflow]))

(defn inc-transform [old-value _]
  ((fnil inc 0) old-value))

(defn value-transform [_ message]
  (:value message))

(defn avg-count [_ inputs]
  (let [counters (dataflow/input-vals inputs)]
    (/ (apply + counters) (count counters))))

(defn init-emitter [_]
  [[:transform-enable [:tutorial] :increment-counter [{msg/type :inc msg/topic [:my-counter]}]]])

(def example-app
  {:version 2
   
   :transform [[:inc [:my-counter] inc-transform]
               [:set-value [:other-counters :*] value-transform]]
   
   :derive #{[#{[:my-counter] [:other-counters :*]} [:avg] avg-count]}
   
   :emit [[#{[:other-counters :*]} (app/default-emitter :tutorial)]
          {:in #{[:*]} :fn (app/default-emitter :tutorial) :init init-emitter}]})
