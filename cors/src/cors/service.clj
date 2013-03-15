; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns cors.service
  (:require [clojure.java.io :as io]
            [io.pedestal.service.interceptor :refer [defhandler defbefore defafter definterceptor]]
            [io.pedestal.service.log :as log]
            [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.body-params :as body-params]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [io.pedestal.service.http.sse :refer [sse-setup send-event end-event-stream]]
            [io.pedestal.service.http.impl.servlet-interceptor :as servlet-interceptor]
            [ring.util.response :as ring-response]
            [ring.middleware.cors :as cors]))

(defn send-thread-id [context]
  (send-event context "thread-id" (str (.getId (Thread/currentThread)))))

(defn thread-id-sender [{{^ServletResponse response :servlet-response
                 :as request} :request :as context}]

  (log/info :msg "starting sending thread id")
  (dotimes [_ 10]
    (Thread/sleep 3000)
    (send-thread-id context))
  (log/info :msg "stopping sending thread id")

  (end-event-stream context))

(defhandler send-js
  "Send the client a response containing the stub JS which consumes an
  event source."
  [req]
  (log/info :msg "returning js")
  (-> (ring-response/response (slurp (io/resource "blob.html")))
      (ring-response/content-type "text/html")))

(definterceptor thread-id-sender (sse-setup thread-id-sender))

(defafter cors-interceptor
  "Interceptor that adds CORS headers when the origin matches the authorized origin."
  [context]
  (let [request (:request context)
        access-control {:access-control-allow-origin #"localhost:8080"}]
    (if (cors/allow-request? request access-control)
      (do (log/debug :msg "allowing request"
                     :request request
                     :access-control access-control)
          (update-in context [:response] #(cors/add-access-control request % access-control)))
      (do (log/debug :msg "not allowing request") context))))

(defbefore cors-options-interceptor
  "Interceptor that adds CORS headers when the origin matches the authorized origin."
  [context]
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
                 (ring-response/header "Access-Control-Allow-Headers" preflight-headers))))))

(defbefore cors-sse-clever-hack-interceptor
  [context]
  (let [request (:request context)
        servlet-response (:servlet-response request)
        access-control {:access-control-allow-origin #"localhost:8080"}
        response (-> (ring-response/response "")
                     (ring-response/content-type "text/event-stream")
                     (ring-response/charset "UTF-8")
                     (ring-response/header "Connection" "close")
                     (ring-response/header "Cache-control" "no-cache")
                     (#(cors/add-access-control request % access-control)))]
    (servlet-interceptor/set-response servlet-response response)
    (.flushBuffer servlet-response)
    context))


(defroutes routes
  [[["/js" {:get send-js} ^:interceptors [cors-interceptor]]
    ["/" ^:interceptors [cors-interceptor cors-sse-clever-hack-interceptor]
     {:get thread-id-sender :options cors-options-interceptor}]]])


;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by cors.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes
              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"
              ;; Choose from [:jetty :tomcat].
              ::bootstrap/type :jetty
              ::bootstrap/port 8081})
