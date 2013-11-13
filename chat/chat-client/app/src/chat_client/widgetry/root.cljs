(ns chat-client.widgetry.root
  (:require [dommy.core :as dommy]
            [chat-client.widgetry.rendering :as r]
            [chat-client.widgetry.log :as l]
            [chat-client.widgetry.widget :as w]
            [chat-client.widgetry.registry :as registry])
  (:use [cljs.core.async :only [chan <! >! put! alts! timeout close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel sel1]]))

(defmulti transform! (fn [_ _ [_ op]] op))

(defmethod transform! :default [context state transformation]
  (w/default-transform! context state transformation))

(defmethod transform! :change-screen [context state [_ _ key id]]
  (registry/remove-all! (:ichan context) #{[:ui :root]})
  (r/clear-all! (:domid context))
  (let [new-domid (keyword (str "#" (name key) "-screen"))]
    (dommy/append! (sel1 (:domid context)) [new-domid])
    (let [create! (get-in context [:widgets key])
          screen-widget (create! id new-domid (:ichan context) :widgets (:widgets context))]
      (registry/add-widget! screen-widget))))

(defn destroy! []
  (l/log "->" "" :i "Root aint neva gonna stop!"))

(defn create! [wid domid ichan & {:keys [widgets]}]
  (let [widget {:wid wid :domid domid :ichan ichan :destroy destroy!}
        tchan (w/start! (assoc widget :widgets widgets) {} transform!)]
    (assoc widget :tchan tchan)))
