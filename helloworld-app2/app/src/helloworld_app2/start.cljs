(ns helloworld-app2.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            [helloworld-app2.rendering :as rendering]
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

(def render-config [[:value [:**] render-value]])

(defn ^:export main []
  (create-app render-config))
