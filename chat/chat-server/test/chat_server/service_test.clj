(ns chat-server.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [chat-server.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest post-to-msgs-endpoint
  (is (=
       (:status (response-for service :post "/msgs" :body "foo"))
       200)))
