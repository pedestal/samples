(ns chat-client.widgets.set-nickname
  (:require [dommy.core :as dommy]
            [chat-client.widgetry.rendering :as r]
            [chat-client.widgetry.registry :as registry]
            [chat-client.widgetry.widget :as w])
  (:require-macros [dommy.macros :refer [sel1]])
  (:use [cljs.core.async :only [put!]]))

;; Necessary?
(defmulti transform! (fn [_ _ [_ op]] op))

(defmethod transform! :default [context state transformation]
  (w/default-transform! context state transformation))

(defn- set-nickname [wid ichan]
  (put! ichan [[wid :set-nickname {:nickname (dommy/value (sel1 :#nickname-input))}]])
  ;; put behind transforms?
  (set! (.-value (sel1 :#nickname-input)) "")
  (registry/remove-widget! wid ichan))

(defn- create-widget! [{:keys [domid wid ichan]}]
  (dommy/add-class! (sel1 :#root) "startup")
  (dommy/remove-class! (sel1 domid) "hide")
  (.focus (sel1 :#nickname-input))
  (r/add-listener! :click domid :#set-nickname-button #(set-nickname wid ichan)))

(defn destroy! [domid]
  (dommy/remove-class! (sel1 :#root) "startup")
  (dommy/add-class! (sel1 domid) "hide")
  (r/remove-all-listeners! domid))

(defn create! [wid domid ichan & args]
  (let [widget {:wid wid :domid domid :ichan ichan :destroy #(destroy! domid)}
        tchan (w/start! widget {} transform!)]
    (create-widget! widget)
    (assoc widget :tchan tchan)))
