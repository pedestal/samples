(ns chat-client.widgets.log
  (:require [dommy.core :as dommy]
            [chat-client.widgetry.rendering :as r]
            [chat-client.widgets.util :as util])
  (:require-macros [dommy.macros :refer [sel1]])
  (:use [cljs.core.async :only [put!]]))

(defn- format-time [d]
  (let [pad (fn [n] (if (< n 10) (str "0" n) (str n)))]
    (str (pad (.getHours d)) ":"
         (pad (.getMinutes d)) ":"
         (pad (.getSeconds d)))))

(defn- create-widget! [{:keys [domid wid ichan options]}]
  (dommy/prepend!
    (sel1 domid)
    [:.message
     [:.gutter]
     [:.body
      [:.header
       [:span.name (:nickname options)]
       [:span.time] (format-time (:time options))]
      [:.content (:text options)]]]))

(def create! (util/create! :create create-widget!
                           ;; TODO
                           :destroy (constantly nil)))
