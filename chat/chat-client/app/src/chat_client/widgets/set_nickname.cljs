(ns chat-client.widgets.set-nickname
  (:require [dommy.core :as dommy]
            [chat-client.widgetry.rendering :as r]
            [chat-client.widgets.util :as util])
  (:require-macros [dommy.macros :refer [sel1]])
  (:use [cljs.core.async :only [put!]]))

(defn- set-nickname [wid ichan]
  (put! ichan [[wid :click {:nickname (dommy/value (sel1 :#nickname-input))}]])
  ;; put behind transform?
  (set! (.-value (sel1 :#nickname-input)) ""))

(defn- create-widget! [{:keys [domid wid ichan]}]
  (dommy/add-class! (sel1 :#root) "startup")
  (dommy/remove-class! (sel1 domid) "hide")
  (.focus (sel1 :#nickname-input))
  (r/add-listener! :click domid :#set-nickname-button #(set-nickname wid ichan)))

(defn destroy! [domid]
  (dommy/remove-class! (sel1 :#root) "startup")
  (dommy/add-class! (sel1 domid) "hide")
  (r/remove-all-listeners! domid))

(def create! (util/create! :create create-widget!
                           :destroy destroy!))
