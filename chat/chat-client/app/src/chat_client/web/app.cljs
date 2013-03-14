(ns chat-client.web.app
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.render.push :as push-render]
            ;; repl-client must be required somewhere in order for the
            ;; fresh view to work.
            [io.pedestal.app.net.repl-client :as repl-client]
            [chat-client.behavior :as behavior]
            [chat-client.web.rendering :as rendering]
            [chat-client.web.services :as services]))

(defn ^:export main []
  (let [app (app/build behavior/chat-client)
        render-fn (push-render/renderer "content" (rendering/render-config) render/log-fn)
        services (services/->Services app)]
    (p/start services)
    (render/consume-app-model app render-fn)
    (app/consume-effects app services/services-fn)
    (app/begin app)
    {:app app}))
