(ns ring-middleware.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [ring-middleware.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest home-page-renders-correctly
  (is (.contains
       (:body (response-for service :get "/"))
       "Enter your name")))
