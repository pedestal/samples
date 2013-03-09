(ns server-with-links.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [server-with-links.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest link-generates-correct-link
  (is (.contains
       (:body (response-for service :get "/"))
       "Go to <a href='/that'>that</a>")))
