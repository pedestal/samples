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

(defn message-produces
  "Given message(s) on the input queue, consumes them and checks assertions on the resulting app state.

  Options:
  * :data-model: Takes a vector pair and checks that the given node has that value. For example, [[:nickname] \"Dude\"] -> [:nickname] path has \"Dude\" value.
  * :deltas: Test the resulting deltas from the emitter.
  * :with-state: Expects a function that is given the resulting state. Useful for doing custom assertions e.g. effect assertions."
  [message & {:keys [node] :as options}]
  (let [app (app/build example-app)]
    (app/begin app)
    (is (vector?
         (test/run-sync! app (if (vector? message) message [message]))))
    (when (:deltas options)
      (is (= (:deltas options)
             (-> app :state deref :io.pedestal.app/emitter-deltas))
          "Emits correct deltas"))
    (when-let [[node value] (:data-model options)]
      (is (= value
             (-> app :state deref :data-model (get-in node)))
          "Sets correct data model value"))
    (when (:with-state options)
      ((:with-state options) (-> app :state deref)))))

(deftest test-set-nickname
  (message-produces
   {msg/type :set-nickname msg/topic [:nickname] :nickname "Mick"}
   :deltas (set-nickname-deltas "Mick")
   :data-model [[:nickname] "Mick"]))

(deftest test-clear-nickname
  (message-produces
    [{msg/type :set-nickname msg/topic [:nickname] :nickname "Mick"}
     {msg/type :clear-nickname msg/topic [:nickname]}]
    :deltas clear-nickname-deltas
    :data-model [[:nickname] nil]))

(deftest test-send-message
  (with-redefs [platform/date (constantly :date)
                util/random-id (constantly 42)]
    (let [msg {:id 42 :time :date :nickname "RR" :text "We have touchdown" :status :pending}]
      (message-produces
        {msg/type :send-message msg/topic [:outbound] :text "We have touchdown" :nickname "RR"}
        :data-model [[:outbound :sent] (list msg)]
        :with-state (fn [state]
                      (is (= [{msg/topic [:server] :out-message msg}]
                             (:effect state))
                          "Sends :server effect message"))))))

(deftest test-clear-outbound-messages
  (message-produces
   {msg/type :clear-messages msg/topic [:outbound]}
   :data-model [[:outbound :sent] []]))

(deftest test-deleted-messages
  (with-redefs [util/random-id (constantly 42)]
    (let [msg {:id 42 :nickname "Apollo" :text "Houston, we have a hotdog"}]
      (message-produces
       [(-> msg (dissoc :id) (assoc msg/type :send-message msg/topic [:outbound]))
        {msg/type :clear-messages msg/topic [:outbound]}]
       :deltas [[:node-destroy [:chat :log 42]]]
       :with-state (fn [state]
                     (is (= ["Apollo"]
                            (map :nickname (get-in state [:data-model :deleted-messages])))))))))

(deftest test-receive-inbound
  (with-redefs [platform/date (constantly :date)]
    (let [msg {:id 42 :nickname "Derp" :text "derp" :time :date}
          msg2 (assoc msg :id 43)]
      (message-produces
       [(-> msg (dissoc :time) (assoc msg/type :received msg/topic [:inbound]))
        (-> msg2 (dissoc :time) (assoc msg/type :received msg/topic [:inbound]))]
        :data-model [[:inbound :received] (list msg2 msg)]
        :deltas [[:node-create [:chat :log 43] :map]
                 [:value [:chat :log 43] msg2]]
        :with-state (fn [state]
                      (is (= (list msg2)
                           (-> state :data-model :new-messages))
                          ":new-messages derives from :inbound"))))))

(deftest test-receive-inbound-sets-updated-message
  (with-redefs [util/random-id (constantly 42)]
    (let [msg {:id 42 :nickname "Hungry Apollo" :text "Houston, we have a bagel"}]
      (message-produces
        [(-> msg (dissoc :id) (assoc msg/type :send-message msg/topic [:outbound]))
         (-> msg (assoc msg/type :received msg/topic [:inbound]))]
        :with-state (fn [state]
                      (is (= (assoc msg :status :confirmed)
                             (-> state
                                 (get-in [:data-model :updated-messages])
                                 first
                                 (dissoc :time)))
                          "message :status is :confirmed")
                      (let [delta (-> state
                                      (get :io.pedestal.app/emitter-deltas)
                                      last)]
                        (is (= [:value [:chat :log 42] msg]
                               (let [[op path actual-msg] delta]
                                 [op path (dissoc actual-msg :time :status)]))
                            "emits updated message delta")))))))

(deftest test-clear-inbound-messages
  (message-produces
    {msg/type :clear-messages msg/topic [:inbound]}
    :data-model [[:inbound :received] []]))

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

