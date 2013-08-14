; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns chat-client.rendering
  (:require [domina :as dom]
            [domina.events :as dom-events]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.events :as events]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.render.push.handlers.automatic :as auto]
            [io.pedestal.app.render.push.handlers :as h])
  (:require-macros [chat-client.html-templates :as html-templates]))

(def templates (html-templates/sample-templates))

(defn render-chat-page [r [_ path] d]
  (let [parent (render/get-parent-id r path)
        html (templates/add-template r path (:chat templates))]
    (dom/append! (dom/by-id parent) (html {:title "Chat"}))))

(defn destroy-chat-page [r [_ path] d]
  (let [parent (render/get-parent-id r path)]
    (dom/destroy-children! (dom/by-id parent))))

(defn send-message-enable [r [_ path transform-name messages] d]
  ;; Remove class hide from .enter-message
  (let [enter-message-form (dom/by-class "enter-message")]
    (dom/remove-class! enter-message-form "hide")
    (.focus (dom/by-id "message-input"))
    (events/send-on :submit
                    enter-message-form
                    d
                    (fn [] (let [text-node (dom/by-id "message-input")
                                 text (.-value text-node)]
                             (set! (.-value text-node) "")
                             (msg/fill transform-name messages {:text text}))))))

(defn send-message-disable [_ _ _]
  (let [enter-message-form (dom/by-class "enter-message")]
    (dom/add-class! enter-message-form "hide")
    (dom-events/unlisten! enter-message-form)))

(defn set-nickname-enable [r [_ path transform-name messages] d]
  (let [enter-nickname-form (dom/by-class "enter-nickname")]
    (dom/add-class! (dom/by-id "root") "startup")
    (dom/remove-class! enter-nickname-form "hide")
    (.focus (dom/by-id "nickname-input"))
    (events/send-on :submit
                    enter-nickname-form
                    d
                    (fn []
                      (let [nickname-node (dom/by-id "nickname-input")
                            nickname (.-value nickname-node)]
                        (set! (.-value nickname-node) "")
                        (msg/fill transform-name messages {:nickname nickname}))))))

(defn set-nickname-disable [_ _ _]
  (let [enter-nickname-form (dom/by-class "enter-nickname")]
    (dom/remove-class! (dom/by-id "root") "startup")
    (dom/add-class! enter-nickname-form "hide")
    (dom-events/unlisten! enter-nickname-form)))

(defn- format-time [d]
  (let [pad (fn [n] (if (< n 10) (str "0" n) (str n)))]
    (str (pad (.getHours d)) ":"
         (pad (.getMinutes d)) ":"
         (pad (.getSeconds d)))))

(defn create-message-node [r [_ path] d]
  (let [id (render/new-id! r path)
        html (templates/add-template r path (:message templates))]
    (templates/prepend-t r [:chat] {:messages (html {:id id :status "pending"})})))

(defn update-message [r [_ path o n] d]
  (let [id (render/get-id r path)
        msg (assoc n :id (:id n) :time (format-time (:time n)))
        msg (if (:status n) (update-in msg [:status] name) msg)]
    (templates/update-t r path msg)))

(defn render-config []
  [[:node-create       [:chat]         render-chat-page]
   [:node-destroy      [:chat]         destroy-chat-page]
   [:transform-enable  [:chat :form :set-nickname] set-nickname-enable]
   [:transform-disable [:chat :form :set-nickname] set-nickname-disable]
   [:transform-enable  [:chat :form :clear-nickname] (h/add-send-on-click (dom/by-class "nickname-icon"))]
   [:transform-disable [:chat :form :clear-nickname] (h/remove-send-on-click (dom/by-class "nickname-icon"))]
   ;; TODO - expose clear-button to user
   [:transform-enable [:chat :form :clear-messages (h/add-send-on-click "clear-button")]]
   [:transform-disable [:chat :form :clear-messages (h/remove-send-on-click "clear-button")]]
   [:transform-enable  [:chat :form :send-message] send-message-enable]
   [:transform-disable [:chat :form :send-message] send-message-disable]
   [:node-create       [:chat :log :*] create-message-node]
   [:node-destroy      [:chat :log :*] auto/default-exit]
   [:value             [:chat :log :*] update-message]])
