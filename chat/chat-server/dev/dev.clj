;; dev mode in repl (can get prod mode by passing prod options to dev-init
(ns dev
  (:require [chat-server.service :as service]
            [chat-server.server :as server]
            [io.pedestal.service.http :as bootstrap]))

(def service (-> service/service
                 (merge  {:env :dev
                          ::bootstrap/join? false
                          ::bootstrap/routes #(deref #'service/routes)})
                 (bootstrap/default-interceptors)
                 (bootstrap/dev-interceptors)))

(defn start
  [& [opts]]
  (server/create-server (merge service opts))
  (bootstrap/start server/service-instance))

(defn stop
  []
  (bootstrap/stop server/service-instance))

(defn restart
  []
  (stop)
  (start))

