; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns chat-client.simulated.services
  (:require [cljs.reader :as r]
            [io.pedestal.app.net.xhr :as xhr]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [chat-client.util :as util]))

;; Notes: The MockServices can be used for testing without a server-side
;; component; it simulates the server by sending new messages to the
;; application every 10 seconds.

(defn receive-messages [app]
  (p/put-message (:input app)
                 {msg/topic [:inbound]
                  msg/type :received
                  :text (str "incoming message " (gensym))
                  :nickname (str (gensym))
                  :id (util/random-id)})
  (.setTimeout js/window (fn [] (receive-messages app)) 10000))

(defrecord MockServices [app]
  p/Activity
  (start [this]
    (receive-messages app))
  (stop [this]))

(defn services-fn [message input-queue]
  (when-let [msg (:out-message message)]
    (.setTimeout js/window #(p/put-message input-queue
                                           {msg/topic [:inbound]
                                            msg/type :received
                                            :text (:text msg)
                                            :nickname (:nickname msg)
                                            :id (:id msg)}) 500)
    (.log js/console (str "Send to Server: " (pr-str message)))))
