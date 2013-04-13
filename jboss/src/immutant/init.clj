(ns immutant.init
    (:require [immutant.web :as web]
              [io.pedestal.service.http :as http]
              [io.pedestal.service.log :as log]
              [jboss.service :as service]))

(def servlet (::http/servlet (http/create-servlet service/service)))

(defn get-context
  []
  (let [context (.getServletContext servlet)
        context-path (when context (.getContextPath context))]
    (log/info :in ::get-context
              :context context
              :context-path context-path)
    context-path))

(web/start-servlet "/" servlet)











