(ns dev
  (:require [io.pedestal.service.http :as bootstrap]
            [auto-compile-server.service :as service]
            [auto-compile-server.server :as server]
            [ns-tracker.core :as tracker]))

(def service (-> service/service ;; start with production configuration
                 (merge  {:env :dev
                          ;; do not block thread that starts web server
                          ::bootstrap/join? false
                          ;; reload routes on every request
                          ::bootstrap/routes #(deref #'service/routes)
                          ;; all origins are allowed in dev mode
                          ::bootstrap/allowed-origins (constantly true)})
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

(defn check-namespace-changes [track]
 (try
   (doseq [ns-sym (track)]
     (require ns-sym :reload)
     (compile ns-sym))
   (catch Throwable e (.printStackTrace e)))
   (Thread/sleep 500))
 
(defn start-nstracker []
  (let [track (tracker/ns-tracker ["src"])
        compile-path *compile-path*]
    (doto
        (Thread.
         (fn []
           (binding [*compile-path* compile-path]
             (println "THREAD" *compile-path*)
             (while true
               (check-namespace-changes track)))))
      (.setDaemon true)
      (.start))))

(defn -main [& args]
  (println *compile-path*)
  (start)
  (start-nstracker))