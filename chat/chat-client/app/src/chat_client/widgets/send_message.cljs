(ns chat-client.widgets.send-message
  (:require [dommy.core :as dommy]
            [chat-client.widgetry.rendering :as r]
            [chat-client.widgets.util :as util])
  (:require-macros [dommy.macros :refer [sel1]])
  (:use [cljs.core.async :only [put!]]))

(defn- send-message [wid ichan]
  (put! ichan [[wid :click {:text (dommy/value (sel1 :#message-input))}]])
  (set! (.-value (sel1 :#message-input)) ""))

(defn- create-widget! [{:keys [domid wid ichan]}]
  (dommy/remove-class! (sel1 domid) "hide")
  (.focus (sel1 :#message-input))
  (r/add-listener! :click domid :#send-message-button #(send-message wid ichan)))

(defn- destroy! [domid]
  (dommy/add-class! (sel1 domid) "hide")
  (r/remove-all-listeners! domid))

(def create! (util/create! :create create-widget!
                           :destroy destroy!))
