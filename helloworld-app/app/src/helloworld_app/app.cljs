; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns helloworld-app.app
  (:require [io.pedestal.app :as app]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.render.push :as push]
            [io.pedestal.app.messages :as msg]
            [domina :as dom]))

(defn count-model [old-state message]
  (condp = (msg/type message)
    msg/init (:value message)
    :inc (inc old-state)))

(defn render-value [r [_ _ old-value new-value] input-queue]
  (dom/destroy-children! (dom/by-id "content"))
  (dom/append! (dom/by-id "content")
               (str "<h1>" new-value " Hello Worlds</h1>")))

(def count-app {:transform {:count {:init 0 :fn count-model}}})

(defn receive-input [input-queue]
  (p/put-message input-queue {msg/topic :count msg/type :inc})
  (.setTimeout js/window #(receive-input input-queue) 3000))

(defn ^:export main []
  (let [app (app/build count-app)
        render-fn (push/renderer "content" [[:value [:**] render-value]])]
    (render/consume-app-model app render-fn)
    (receive-input (:input app))
    (app/begin app)))

