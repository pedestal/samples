(ns tutorial-service.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [ring.util.response :as ring-resp]
              [io.pedestal.service.http.sse :as sse]
              [io.pedestal.service.log :as log]
              [io.pedestal.service.http.ring-middlewares :as middlewares]
              [io.pedestal.service.interceptor :refer [definterceptor]]
              [ring.middleware.session.cookie :as cookie]))

(def ^:private streaming-contexts (atom {}))

(defn- session-from-context
  "Extract the session id from the streaming context."
  [streaming-context]
  (get-in streaming-context [:request :cookies "client-id" :value]))

(defn- session-from-request
  "Extract the session id from a request."
  [request]
  (get-in request [:cookies "client-id" :value]))

(defn- clean-up
  "Remove the given streaming context and shutdown the event stream."
  [streaming-context]
  (swap! streaming-contexts dissoc (session-from-context streaming-context))
  (sse/end-event-stream streaming-context))

(defn- notify
  "Send event-data to the connected client."
  [session-id event-name event-data]
  (when-let [streaming-context (get @streaming-contexts session-id)]
    (try
      (sse/send-event streaming-context event-name event-data)
      (catch java.io.IOException ioe
        (clean-up streaming-context)))))

(defn- notify-all-others
  "Send event-data to all connected channels except for the given session-id."
  [sending-session-id event-name event-data]
  (doseq [session-id (keys @streaming-contexts)]
    (when (not= session-id sending-session-id)
      (notify session-id event-name event-data))))

(defn- store-streaming-context [streaming-context]
  (let [session-id (session-from-context streaming-context)]
    (swap! streaming-contexts assoc session-id streaming-context)))

(defn- session-id [] (.toString (java.util.UUID/randomUUID)))

(declare url-for)

(defn subscribe
  "Assign a session cookie to this request if one does not
  exist. Redirect to the events channel."
  [request]
  (let [session-id (or (session-from-request request)
                       (session-id))
        cookie {:client-id {:value session-id :path "/"}}]
    (-> (ring-resp/redirect (url-for ::events))
        (update-in [:cookies] merge cookie))))

(definterceptor session-interceptor
  (middlewares/session {:store (cookie/cookie-store)}))

(defn publish
  "Publish a message to all other connected clients."
  [{msg-data :edn-params :as request}]
  (log/info :message "received message"
            :request request
            :msg-data msg-data)
  (let [session-id (session-from-request request)]
    (notify-all-others session-id
                       "msg"
                       (pr-str (update-in msg-data
                                          [:io.pedestal.app.messages/topic]
                                          conj
                                          (subs session-id 0 8)))))
  (ring-resp/response ""))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body session-interceptor]
     ["/about" {:get about-page}]
     ["/msgs" {:get subscribe :post publish}
      ["/events" {:get [::events (sse/start-event-stream store-streaming-context)]}]]]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by tutorial-service.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::boostrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
