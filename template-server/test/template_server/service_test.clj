(ns template-server.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [template-server.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest test-templates-generate-correct-bodies
  (are [url partial-body-string]
       (.contains (->> url
                       (response-for service :get)
                       :body)
                  partial-body-string)
       "/hiccup" "<p>Hello from Hiccup</p>"
       "/enlive" "<p id=\"the-text\">Hello from the Enlive demo page. Have a nice day!</p>"
       "/mustache" "<p id=\"the-text\">Hello from the Mustache demo page. Have a great day!</p>"
       "/stringtemplate" "<h1>Hello from String Template</h1>"
       "/comb" "<p>This is not erb</p>"))
