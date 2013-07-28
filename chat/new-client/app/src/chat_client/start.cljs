(ns chat-client.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            [chat-client.behavior :as behavior]
            [chat-client.rendering :as rendering]))

(defn create-app [render-config]
  (let [app (app/build behavior/example-app)
        render-fn (push-render/renderer "content" render-config render/log-fn)
        app-model (render/consume-app-model app render-fn)]
    (app/begin app)
    {:app app :app-model app-model}))

(defn setup-services [app ->services services-fn]
  (app/consume-effects (:app app) services-fn)
  (p/start (->services (:app app))))

(defn ^:export main []
  (create-app (rendering/render-config)))
