(ns chat-client.widgets.login
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

(defn- send-login! [wid ichan]
  (let [uid (dommy/value (sel1 :#login-email))
        password (dommy/value (sel1 :#login-password))]
    (put! ichan [[wid :submit {:uid uid :pw password}]])))

(defmethod transform! :authenticating [context state [_ _ uid]]
  (r/clear-all! :#login-form)
  (dommy/append! (sel1 :#login-form) [:.authenticating
                                      [:h1 "Authenticating... "]
                                      [:h2 uid]])
  state)

(defn- create-widget! [{:keys [domid wid ichan]}]
  (dommy/append! (sel1 domid)
                 [:.inset-panel
                  [:.center-heading
                   [:h1 "The Counter"]]
                  [:#login-form template]])
  (r/add-listener! :click :#login-form :#login-submit #(send-login! wid ichan)))

(defn destroy! [domid]
  (r/clear-all! :#login-form)
  (r/remove-all! domid))

(defn create! [wid domid ichan & args]
  (let [widget {:wid wid :domid domid :ichan ichan :destroy #(destroy! domid)}
        tchan (w/start! widget {} transform!)]
    (create-widget! widget)
    (assoc widget :tchan tchan)))
