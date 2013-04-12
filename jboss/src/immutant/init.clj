(ns immutant.init
    (:require [immutant.web :as web]
              [io.pedestal.service.http :as http]
              [jboss.service :as service]))

(def servlet (::http/servlet (http/create-servlet service/service)))

(web/start-servlet "/" servlet)

;;(service/init-url-for (.. servlet getServletContext getContextPath))










