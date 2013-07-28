(ns chat-client.simulated.start
  (:require [io.pedestal.app.render.push.handlers.automatic :as d]
            [chat-client.start :as start]
            [chat-client.simulated.services :as services]
            ;; This needs to be included somewhere in order for the
            ;; tools to work.
            [io.pedestal.app-tools.tooling :as tooling]))

(defn ^:export main []
  (doto (start/create-app d/data-renderer-config)
    (start/setup-services services/->MockServices services/services-fn)))
