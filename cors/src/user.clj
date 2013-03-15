; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns user
  (:require [pedestal.service.http.servlet :as ps]
            [io.pedestal.app :as app]
            [pedestal.service.log :as log]
            [pedestal.service.interceptor :as interceptor :refer [definterceptor]]
            [pedestal.service.http.impl.servlet-interceptor :as servlet-interceptor]
            [pedestal.service.http.route :refer [router]]
            [pedestal.service.http.route.definition :refer [defroutes]]
            [pedestal.service.http.tomcat :refer [tomcat]]
            [ring.util.response :as ring-response]
            [ring.middleware.cors :as cors]
            [pedestal.service.http.sse :refer :all]
            [clojure.java.io :as io]))

;; want handler to get js file
;; move app-level streaming code up here


(defn send-thread-id [context]
  (event context "thread-id" (str (.getId (Thread/currentThread)))))

(defn thread-id-sender [{{^ServletResponse response :servlet-response
                 :as request} :request :as context}]

  (log/info :msg "starting sending thread id")
  (dotimes [_ 10]
    (Thread/sleep 3000)
    (send-thread-id context))
  (log/info :msg "stopping sending thread id")

  ((:end-event-stream context)))

(defn send-js
  "Send the client a response containing the stub JS which consumes an
  event source."
  [context]
  (log/info :msg "returning js")
  (assoc context :response (-> (ring-response/response (slurp (io/resource "blob.html")))
                               (ring-response/content-type "text/html"))))

(definterceptor send-js (interceptor/interceptor :enter send-js))

(definterceptor thread-id-sender (sse-interceptor thread-id-sender))

(definterceptor cors-interceptor
  "Interceptor that adds CORS headers when the origin matches the authorized origin."
  (interceptor/after
   (fn [context]
     (let [request (:request context)
           access-control {:access-control-allow-origin #"localhost:3000"}]
       (if (cors/allow-request? request access-control)
         (do (log/debug :msg "allowing request"
                        :request request
                        :access-control access-control)
             (update-in context [:response] #(cors/add-access-control request % access-control)))
         (do (log/debug :msg "not allowing request") context))))))

(definterceptor cors-options-interceptor
  "Interceptor that adds CORS headers when the origin matches the authorized origin."
  (interceptor/before
   (fn [context]
     ;; special case options request
     (let [request (:request context)
           _ (log/debug :msg "options request headers" :headers (:headers request))
           preflight-origin (get-in request [:headers "origin"])
           preflight-headers (get-in request [:headers "access-control-request-headers"])]
       (log/debug :msg "access-control-request-headers" :preflight-headers preflight-headers)
       (assoc context :response
              (when (= (:request-method request) :options)
                (-> (ring-response/response "")
                    (ring-response/header "Access-Control-Allow-Origin" preflight-origin)
                    (ring-response/header "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    (ring-response/header "Access-Control-Allow-Headers" preflight-headers))))))))


(defroutes routes
  [[["/js" {:get send-js} ^:interceptors [cors-interceptor]]
    ["/" ^:interceptors [cors-interceptor]
     {:get thread-id-sender :options cors-options-interceptor}]]])


(defn go
  ([] (go 3000))
  ([port]
     (println "Go!")
     (.start (tomcat port (service/routing-servlet routes)))))
