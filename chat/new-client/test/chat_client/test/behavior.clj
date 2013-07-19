(ns chat-client.test.behavior
  (:require [io.pedestal.app :as app]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.tree :as tree]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.util.test :as test])
  (:use clojure.test
        chat-client.behavior
        [io.pedestal.app.query :only [q]]))

;; Test a transform function

(deftest test-set-value-transform
  (is (= (nickname-transform {} {msg/type :set-nickname msg/topic [:nickname] :nickname "x"})
         "x")))

;; Build an application, send a message to a transform and check the transform
;; state

(defn message-generates-deltas
  [message deltas & {:keys [node] :as options}]
  (let [app (app/build example-app)]
    (app/begin app)
    (is (vector?
         (test/run-sync! app [message])))
    (is (= deltas
           (-> app :state deref :io.pedestal.app/emitter-deltas))
        "Emits correct deltas")
    (is (= (:value options)
           (-> app :state deref :data-model node))
        "Modifies correct data model value")))

(deftest test-set-nickname
  (message-generates-deltas
   {msg/type :set-nickname msg/topic [:nickname] :nickname "Mick"}
   (set-nickname-deltas "Mick")
   :node :nickname
   :value "Mick"))

(deftest test-clear-nickname
  (message-generates-deltas
   {msg/type :clear-nickname msg/topic [:nickname]}
   clear-nickname-deltas
   :node :nickname
   :value nil))

;; Use io.pedestal.app.query to query the current application model

(deftest test-query-ui
  (let [app (app/build example-app)
        app-model (render/consume-app-model app (constantly nil))]
    (app/begin app)
    (is (test/run-sync! app [{msg/topic [:nickname] msg/type :set-nickname :nickname "x"}]))
    (is (= (q '[:find ?v
                :where
                [?n :t/path [:chat :nickname]]
                [?n :t/value ?v]]
              @app-model)
           [["x"]]))))

