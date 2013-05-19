(ns tutorial-client.services
  (:require [io.pedestal.app.protocols :as p]
            [cljs.reader :as reader]
            [io.pedestal.app.net.xhr :as xhr]))

(defn receive-ss-event [app e]
  (let [message (reader/read-string (.-data e))]
    (p/put-message (:input app) message)))

(defrecord Services [app]
  p/Activity
  (start [this]
    (let [source (js/EventSource. "/msgs")]
      (.addEventListener source
                         "msg"
                         (fn [e] (receive-ss-event app e))
                         false)
      (.addEventListener source
                         "open"
                         (fn [e] (.log js/console e))
                         false)
      (.addEventListener source
                         "error"
                         (fn [e] (.log js/console e))
                         false)
      (.log js/console source)))
  (stop [this]))

(defn services-fn [message input-queue]
  (let [body (pr-str message)
        log (fn [args]
              (.log js/console (pr-str args))
              (.log js/console (:xhr args)))]
    (xhr/request (gensym)
                 "/msgs"
                 :request-method "POST"
                 :headers {"Content-Type" "application/edn"}
                 :body body
                 :on-success log
                 :on-error log)
    (.log js/console (str "Send to Server: " (pr-str message)))))

