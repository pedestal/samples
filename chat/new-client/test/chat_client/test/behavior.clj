(ns chat-client.test.behavior
  (:require [io.pedestal.app :as app]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.tree :as tree]
            [io.pedestal.app.util.platform :as platform]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.util.test :as test]
            [chat-client.util :as util])
  (:use clojure.test
        chat-client.behavior
        [io.pedestal.app.query :only [q]]))

;; Test a transform function

(deftest test-nickname-transform
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
           (-> app :state deref :data-model (get-in node)))
        "Modifies correct data model value")
    (when (:with-state options)
      ((:with-state options) (-> app :state deref)))))

(deftest test-set-nickname
  (message-generates-deltas
   {msg/type :set-nickname msg/topic [:nickname] :nickname "Mick"}
   (set-nickname-deltas "Mick")
   :node [:nickname]
   :value "Mick"))

(deftest test-clear-nickname
  (message-generates-deltas
   {msg/type :clear-nickname msg/topic [:nickname]}
   clear-nickname-deltas
   :node [:nickname]
   :value nil))

(deftest test-send-message
  (with-redefs [platform/date (constantly :date)
                util/random-id (constantly 42)]
    (let [msg {:id 42 :time :date :nickname "RR" :text "We have touchdown" :status :pending}]
      (message-generates-deltas
        {msg/type :send-message msg/topic [:outbound] :text "We have touchdown" :nickname "RR"}
        []
        :node [:outbound :sent]
        :value (list msg)
        :with-state (fn [state]
                      (is (= [{msg/topic [:server] :out-message msg}]
                             (:effect state))
                          "Sends :server effect message"))))))

(deftest test-clear-outbound-messages
  (message-generates-deltas
    {msg/type :clear-messages msg/topic [:outbound]}
    []
    :node [:outbound]
    :value {:sent []}))

(deftest test-receive-inbound
  (with-redefs [platform/date (constantly :date)]
    (let [msg {:id 42 :nickname "Derp" :text "derp" :time :date}]
      (message-generates-deltas
      (merge {msg/type :received msg/topic [:inbound]} (dissoc msg :time))
      []
      :node [:inbound :received]
      :value (list msg)))))

(deftest test-clear-inbound-messages
  (message-generates-deltas
    {msg/type :clear-messages msg/topic [:inbound]}
    []
    :node [:inbound]
    :value {:received []}))
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

