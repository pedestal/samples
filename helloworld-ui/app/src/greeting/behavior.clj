; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns ^:shared greeting.behavior
    (:require [io.pedestal.app.protocols :as p]
              [io.pedestal.app.messages :as msg]
              [io.pedestal.app :as app]))

(defn greeting-transform-fn [state event]
  (if (= (msg/type event) msg/init) (:value event) (:input event)))


;; Emits

(defn greeting-emit-fn
  ([inputs]
     [{:app {:greeting  {}}}])
  ([inputs changed-inputs]
     [[:value [:app :greeting] (:new (get inputs (first changed-inputs)))]]))

(def greeting-app
  {:transform {:greeting-transform {:init "THIS IS NEVER USED" :fn greeting-transform-fn}}
   :emit {:greeting-emit {:fn greeting-emit-fn :input #{:greeting-transform}}}})

(comment

  (defn print-renderer [out]
    (fn [deltas input-queue]
      (binding [*out* out]
        (println "---------")
        (doseq [d deltas]
          (println "Render: " d)))))
  
  (def app (app/build greeting-app))
  (render/consume-app-model app (print-renderer *out*))
  (app/begin app)

  (p/put-message (:input app)
                 {msg/topic :greeting-transform msg/type :greeting-event :input "Have a good one."})
  (p/put-message (:input app)
                 {msg/topic :greeting-transform msg/type :something :input "How're you doing?"})

  )
