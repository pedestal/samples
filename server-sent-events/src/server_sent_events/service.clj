(ns server-sent-events.service
  (:require [clojure.core.async :as async]
            [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.sse :as sse]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]))

(defn send-counter
  "Counts down to 0, sending value of counter to sse context and
  recursing on a different thread; ends event stream when counter
  is 0."
  [channel count]
  (async/go
   (async/>! channel (str count ", thread: " (.getId (Thread/currentThread))))
   (<! (async/timeout 2000))
   (if (> count 0)
     (future (send-counter channel (dec count)))
     (async/close! channel))))

(defn sse-stream-ready
  "Starts sending counter events to client."
  [channel]
  (send-counter channel 10))

;; Wire root URL to sse event stream
(defroutes routes
  [[["/" {:get [::send-counter (sse/sse-setup sse-stream-ready)]}]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by server-sent-events.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes
              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"
              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
