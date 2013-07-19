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

(deftest test-app-state
  (let [app (app/build example-app)]
    (app/begin app)
    (is (vector?
         (test/run-sync! app [{msg/type :set-nickname msg/topic [:nickname] :nickname "Mick"}])))
    (is (= [[:node-create [:chat :nickname] :map]
            [:value [:chat :nickname] "Mick"]
            [:transform-enable [:chat :form] :clear-nickname [{msg/topic [:nickname]}]]
            [:transform-enable [:chat :form] :send-message [{msg/topic [:outbound]
                                                             (msg/param :text) {}
                                                             :nickname "Mick"}]]
            [:transform-disable [:chat :form] :set-nickname]]
           (-> app :state deref :io.pedestal.app/emitter-deltas))
        "Emits correct deltas")
    (is (= (-> app :state deref :data-model :nickname) "Mick")
        "Modifies correct data model value")))

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

