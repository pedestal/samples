(ns chat-server.service
  (:require [io.pedestal.service.http.servlet :as ps]
            [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.log :as log]
            ;; the impl dependencies will go away
            [io.pedestal.service.impl.interceptor :as interceptor]
            [io.pedestal.service.interceptor :refer [definterceptor handler]]
            [io.pedestal.service.http.impl.servlet-interceptor :as servlet-interceptor]
            [io.pedestal.service.http.ring-middlewares :as middlewares]
            [io.pedestal.service.http.body-params :as body-params]
            ;; these next two will collapse to one
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-response]
            [io.pedestal.service.http.sse :refer :all]
            [ring.util.mime-type :as ring-mime]
            [ring.middleware.session.cookie :as cookie]))

(def ^{:doc "Map of subscriber IDs to SSE contexts"} subscribers (atom {}))

(defn context-key
  "Return key for given `context`."
  [sse-context]
  (get-in sse-context [:request :cookies "chat-id" :value]))

(defn add-subscriber
  "Add `context` to subscribers map."
  [sse-context]
  (swap! subscribers assoc (context-key sse-context) sse-context))

(defn remove-subscriber
  "Remove `context` from subscribers map and end the event stream."
  [context]
  (log/info :msg "removing subscriber")
  (swap! subscribers dissoc (context-key context))
  (end-event-stream context))

(def ^{:doc "Interceptor used to add subscribers."}
  wait-for-events (sse-setup add-subscriber))

(defn send-to-subscriber
  "Send `msg` as event to event stream represented by `context`. If
  send fails, removes `context` from subscribers map."
  [context msg]
  (try
    (log/info :msg "calling event sending fn")
    (send-event context "msg" msg)
    (catch java.io.IOException ioe
      (log/error :msg "Exception from event send"
                 :exception ioe)
      (remove-subscriber context))))

(defn send-to-subscribers
  "Send `msg` to all event streams in subscribers map."
  [msg]
  (log/info :msg "sending to all subscribers")
  (doseq [sse-context (vals @subscribers)]
    (send-to-subscriber sse-context msg)))

(defn publish
  "Terminal interceptor for publishing msg to subscribers."
  [{msg-data :edn-params :as request}]
  (log/info :message "publishing msg"
            :request request
            :msg-data msg-data)
  (when msg-data
    ;; pr-str won't be needed in the future
    (send-to-subscribers (pr-str msg-data)))
  (ring-response/response ""))


(defn- session-id [] (.toString (java.util.UUID/randomUUID)))

(declare url-for)

(defn subscribe
  [request]
     (let [session-id (or (get-in request [:cookies "chat-id" :value])
                          (session-id))
           cookie {:chat-id {:value session-id :path "/"}}]
       (-> (ring-response/redirect (url-for ::wait-for-events))
           (update-in [:cookies] merge cookie))))

(definterceptor session-interceptor
  (middlewares/session {:store (cookie/cookie-store)}))

;; define service routes
(defroutes routes
  [[["/" ^:interceptors [body-params/body-params session-interceptor]
     ["/msgs" {:get subscribe :post publish}
      ["/events" {:get wait-for-events}]]]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by chat-server.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ::bootstrap/routes routes
              ;; Root for resource interceptor that is available by default.
              ;;              ::bootstrap/resource-path nil
              ;; Choose from [:jetty :tomcat].
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})

