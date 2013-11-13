(ns chat-client.widgets.chat
  (:require [dommy.core :as dommy]
            [chat-client.widgets.set-nickname :as set-nickname]
            [chat-client.widgets.send-message :as send-message]
            [chat-client.widgets.util :as util]
            [chat-client.widgetry.rendering :as r]
            [chat-client.widgetry.registry :as registry]
            [chat-client.widgetry.widget :as w])
  (:require-macros [dommy.macros :refer [sel1]])
  (:use [cljs.core.async :only [put!]]))

(defmulti transform! (fn [_ _ [_ op]] op))

(defmethod transform! :default [context state transformation]
  (w/default-transform! context state transformation))

(def template
  [:#root.startup
   [:.chat-wrapper
    [:.chat-frame
     [:.messages
      [:.message
       [:.gutter]
       [:.body
        [:.header
         [:span.name "anonymous"]
         [:span.time] "- 10:23 PM"]
        [:.content "Filler"]]]]
     [:.chat-footer]]
    [:.chat-bar
     [:form {:class "enter-message hide"}
      [:.right
       [:i.nickname-icon]
       [:button#send-message-button.send-button {:type "button"}]]
      [:i {:class "message-icon focused"}]
      [:input#message-input {:type "text" :placeholder "Enter a message..."}]]
     [:form.enter-nickname
      [:.right
       [:button#set-nickname-button.ok-button {:type "submit"} "OK!"]
       [:.tooltip "I know you’re excited but please enter a nickname first."]
       [:i {:class "nickname-icon focused"}]
       [:input#nickname-input {:type "text" :placeholder "Enter your nickname..."}]]]
     ]]])

(defmethod transform! :nickname-set [{:keys [ichan]} state _]
  (registry/remove-widget! [:ui :set-nickname] ichan)
  (registry/add-widget! (send-message/create! [:ui :send-message] :form.enter-message ichan))
  ;; TODO: enable clear-nickname
  state)

(defn- create-widget! [{:keys [domid wid ichan]}]
  (dommy/append! (sel1 domid) template)
  (registry/add-widget! (set-nickname/create! [:ui :set-nickname] :form.enter-nickname ichan)))

(defn destroy! [domid]
  #_(r/remove-all! domid))

(def create! (util/create! :create create-widget!
                           :destroy destroy!
                           :transform transform!))
