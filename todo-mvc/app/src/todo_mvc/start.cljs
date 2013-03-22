(ns todo-mvc.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [todo-mvc.behavior :as behavior]
            [todo-mvc.rendering :as rendering]))

;; In this namespace, the application is built and started.

(defn create-app [render-config]
  (let [app (app/build behavior/todo-app)
        render-fn (push-render/renderer "content" render-config render/log-fn)
        app-model (render/consume-app-model app render-fn)]
    (app/begin app)
    {:app app :app-model app-model}))

(defn ^:export main []
  (create-app (rendering/render-config)))
