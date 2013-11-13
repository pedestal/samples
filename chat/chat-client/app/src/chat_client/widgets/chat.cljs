(ns chat-client.widgets.chat
  (:require [dommy.core :as dommy]
            [chat-client.widgetry.rendering :as r]
            [chat-client.widgetry.widget :as w])
  (:require-macros [dommy.macros :refer [sel1]])
  (:use [cljs.core.async :only [put!]]))

(defmulti transform! (fn [_ _ [_ op]] op))

(defmethod transform! :default [context state transformation]
  (w/default-transform! context state transformation))

(def template
  [:div
   [:h2 "Login"]
   [:form {:role "form"}
    [:.form-group
     [:input#login-email.form-control {:type "email" :placeholder "Email Address"}]]
    [:.form-group
     [:input#login-password.form-control {:type "password" :placeholder "Password"}]]
    [:button#login-submit {:type "submit" :class "btn btn-primary btn-block"} "Submit"]]])

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

#_(defn- send-login! [wid ichan]
  (let [uid (dommy/value (sel1 :#login-email))
        password (dommy/value (sel1 :#login-password))]
    (put! ichan [[wid :submit {:uid uid :pw password}]])))

#_(defmethod transform! :authenticating [context state [_ _ uid]]
  (r/clear-all! :#login-form)
  (dommy/append! (sel1 :#login-form) [:.authenticating
                                      [:h1 "Authenticating... "]
                                      [:h2 uid]])
  state)

(defn- create-widget! [{:keys [domid wid ichan]}]
  (dommy/append! (sel1 domid)
                 template)
  #_(r/add-listener! :click :#login-form :#login-submit #(send-login! wid ichan)))

(defn destroy! [domid]
  (r/clear-all! :#login-form)
  (r/remove-all! domid))

(defn create! [wid domid ichan & args]
  (let [widget {:wid wid :domid domid :ichan ichan :destroy #(destroy! domid)}
        tchan (w/start! widget {} transform!)]
    (create-widget! widget)
    (assoc widget :tchan tchan)))
