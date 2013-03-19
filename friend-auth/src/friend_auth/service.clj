(ns friend-auth.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [io.pedestal.service.http.ring-middlewares :as middlewares]
              [io.pedestal.service.interceptor :refer [definterceptorfn definterceptor interceptor]]
              [ring.util.response :as ring-resp]
              [ring.middleware.session.cookie :as cookie]
              [cemerick.friend :as friend]
              (cemerick.friend [workflows :as workflows]
                               [credentials :as creds])))

;; Views
(defn secure-page
  [request]
  (friend/handler-request
   (fn [inner-request]
     (friend/authenticated
      (ring-resp/response (format "You're logged in as %s"
                                  (friend/current-authentication)))))
   (:friend/handler-map request)))

;; Auth
(def users (atom {"friend" {:username "friend"
                            :password (creds/hash-bcrypt "clojure")
                            :roles #{::user}}
                  "friend-admin" {:username "friend-admin"
                                  :password (creds/hash-bcrypt "clojure")
                                  :roles #{::admin}}}))
(derive ::admin ::user)

;; Interceptors
(definterceptor session-interceptor
  (middlewares/session {:store (cookie/cookie-store)}))

;; TODO: move to a separate library once friend fork has been merged
(definterceptorfn friend-authenticate-interceptor
  "Creates a friend interceptor for friend/authenticate given a config."
  [auth-config]
  (interceptor
   :enter (fn [{request :request :as context}]
            (let [response-or-handler-map (friend/authenticate-request request auth-config)]
              (if-let [handler-map (:friend/handler-map response-or-handler-map)]
                (assoc context :request (assoc request :friend/handler-map handler-map))
                (assoc context :response response-or-handler-map))))
   :leave (middlewares/response-fn-adapter friend/authenticate-response)))

(def friend-interceptor
  (friend-authenticate-interceptor
   {:allow-anon? true
    :unauthenticated-handler #(workflows/http-basic-deny "Pedestal demo" %)
    :workflows [(workflows/http-basic
                 :credential-fn #(creds/bcrypt-credential-fn @users %)
                 :realm "Pedestal demo")]}))

(defroutes routes
  [[["/secure" ^:interceptors [session-interceptor friend-interceptor] {:get secure-page}]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by friend-auth.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes
              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"
              ::bootstrap/file-path "resources/public"
              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
