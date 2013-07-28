(ns chat-client.simulated.start
  (:require [io.pedestal.app.render.push.handlers.automatic :as d]
            [chat-client.start :as start]
            [chat-client.rendering :as rendering]
            [chat-client.simulated.services :as services]
            ;; This needs to be included somewhere in order for the
            ;; tools to work.
            [io.pedestal.app-tools.tooling :as tooling]))

(defn ^:export main []
  (let [uri (goog.Uri. (.toString (.-location js/document)))
        renderer (.getParameterValue uri "renderer")
        render-config (if (= renderer "auto")
                        d/data-renderer-config
                        (rendering/render-config))]
    (doto (start/create-app render-config)
      (start/setup-services services/->MockServices services/services-fn))))
