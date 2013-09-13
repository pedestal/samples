; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns chat-client.services
  (:require [cljs.reader :as r]
            [io.pedestal.app.net.xhr :as xhr]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.log :as log]
            [chat-client.util :as util]))

(defrecord Services [app]
  p/Activity
  (start [this]
    (let [source (js/EventSource. "/msgs")]
      (.addEventListener source
                         "msg"
                         (fn [e]
                           (let [data (r/read-string (.-data e))]
                             (.log js/console e)
                             (p/put-message (:input app)
                                            {msg/topic [:inbound]
                                             msg/type :received
                                             :text (:text data)
                                             :nickname (:nickname data)
                                             :id (util/random-id)}))) ; TODO: track msg id throughout the system
                         false)
      (.addEventListener source
                         "open"
                         (fn [e]
                           (.log js/console e))
                         false)
      (.addEventListener source
                         "error"
                         (fn [e]
                           (.log js/console e))
                         false)
      (.log js/console source)))
  (stop [this]))

(defn services-fn [message _]
  (when-let [msg (:out-message message)]
    (let [body (pr-str {:text (:text msg) :nickname (:nickname msg)})
          log (fn [args]
                (.log js/console (pr-str args))
                (.log js/console (:xhr args)))]
      (xhr/request (gensym)
                   "/msgs"
                   :request-method "POST"
                   :headers {"Content-Type" "application/edn"}
                   :body body
                   :on-success log
                   :on-error log))
    (.log js/console (str "Send to Server: " (pr-str message)))))
