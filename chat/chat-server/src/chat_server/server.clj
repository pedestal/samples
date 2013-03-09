(ns chat-server.server
  (:require [chat-server.service :as service]
            [io.pedestal.service.http :as bootstrap]))

(def service-instance
  "Global var to hold service instance."
  nil)

(defn create-server
  "Standalone dev/prod mode."
  [& [opts]]
  (alter-var-root #'service-instance
                  (constantly (bootstrap/create-server (merge service/service opts)))))

(defn -main [& args]
  (create-server)
  (bootstrap/start service-instance))


;; Container prod mode for use with the pedestal.servlet.ClojureVarServlet class.

(defn servlet-init [this config]
  (require 'chat-server.service)
  (alter-var-root #'service-instance (bootstrap/create-servlet service/service))
  (bootstrap/start service-instance)
  (.init (::bootstrap/servlet service-instance) config))

(defn servlet-destroy [this]
  (bootstrap/stop service-instance)
  (alter-var-root #'service-instance nil))

(defn servlet-service [this servlet-req servlet-resp]
  (.service ^javax.servlet.Servlet (::bootstrap/servlet service-instance)
            servlet-req servlet-resp))
