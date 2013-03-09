(ns greeting.app
  (:require [greeting.behavior :as behavior]
            [greeting.rendering :as rendering]
            [greeting.services :as services]
            [io.pedestal.app :as app]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.net.repl-client :as repl-client]))

;; Start

(defn ^:export main []
  (let [app (app/build behavior/greeting-app)
        render-fn (push-render/renderer "content" rendering/render-config render/log-fn)]
    (services/receive-messages app)
    (render/consume-app-model app render-fn)
    (app/begin app)))
