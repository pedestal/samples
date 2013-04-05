(ns immutant.init
    (:require [immutant.web :as web]
              [io.pedestal.service.http :as http]
              [jboss.service :as service]))

(web/start-servlet "/"
                   (::http/servlet (http/create-servlet service/service)))










