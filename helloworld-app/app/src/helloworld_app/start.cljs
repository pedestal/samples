; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns helloworld-app.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            [domina :as dom]))

(defn inc-transform [old-value _]
  ((fnil inc 0) old-value))

(def count-app {:version 2
                :transform [[:inc [:count] inc-transform]]})

(defn receive-input [input-queue]
  (p/put-message input-queue {msg/topic [:count] msg/type :inc})
  (.setTimeout js/window #(receive-input input-queue) 3000))

(defn create-app [render-config]
  (let [app (app/build count-app)
        render-fn (push-render/renderer "content" render-config render/log-fn)
        app-model (render/consume-app-model app render-fn)]
    (app/begin app)
    (receive-input (:input app))
    {:app app :app-model app-model}))

(defn render-value [r [_ _ old-value new-value] input-queue]
  (dom/destroy-children! (dom/by-id "content"))
  (dom/append! (dom/by-id "content")
               (str "<h1>" new-value " Hello Worlds</h1>")))

(defn render-config []
  [[:value [:**] render-value]])

(defn ^:export main []
  (create-app (render-config)))
