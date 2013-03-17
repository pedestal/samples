(ns friend-auth.core
  (:require [pedestal.server :as server]
            [pedestal.route.definition.terse :as terse]
            [pedestal.middlewares :as middlewares]
            [pedestal.interceptor :refer [definterceptor definterceptorfn] :as interceptor]
            [ring.middleware.session.cookie :as cookie]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

;; Views
(def secure-page
  (server/response
   (fn [request]
     (friend/handler-request
      (fn [inner-request]
        (friend/authenticated
         (format "You're logged in as %s"
                 (friend/current-authentication))))
      (:friend/handler-map request)))))

;; Auth
(def users (atom {"friend" {:username "friend"
                            :password (creds/hash-bcrypt "clojure")
                            :roles #{::user}}
                  "friend-admin" {:username "friend-admin"
                                  :password (creds/hash-bcrypt "clojure")
                                  :roles #{::admin}}}))

(derive ::admin ::user)

;; TODO: move to a separate library once friend fork has been merged
(definterceptorfn friend-authenticate-interceptor
  "Creates a friend interceptor for friend/authenticate given a config."
  [auth-config]
  (interceptor/interceptor
   :enter (fn [{request :request :as context}]
            (let [response-or-handler-map (friend/authenticate-request request auth-config)]
              (if-let [handler-map (:friend/handler-map response-or-handler-map)]
                (assoc context :request (assoc request :friend/handler-map handler-map))
                (assoc context :response response-or-handler-map))))
   
   :leave (middlewares/response-fn-adaptor friend/authenticate-response)))

(def friend-interceptor
  (friend-authenticate-interceptor
   {:allow-anon? true
    :unauthenticated-handler #(workflows/http-basic-deny "Pedestal demo" %)
    :workflows [(workflows/http-basic
                 :credential-fn #(creds/bcrypt-credential-fn @users %)
                 :realm "Pedestal demo")]}))

(definterceptor session-interceptor
  (middlewares/session-interceptor {:store (cookie/cookie-store)}))

(terse/defroutes app
  [[
    ["/secure" ^:interceptors [session-interceptor friend-interceptor] {:get secure-page}]]])

(defn -main [& args]
  (server/start app :file-path "resources/public"))