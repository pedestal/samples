(ns chat-client.widgets.clear-nickname
  (:require [chat-client.widgetry.rendering :as r]
            [chat-client.widgets.util :as util])
  (:use [cljs.core.async :only [put!]]))

(defn- create-widget! [{:keys [domid wid ichan]}]
  (r/add-listener! :click domid domid #(put! ichan [[wid :click]])))

(def create! (util/create! :create create-widget!
                           :destroy r/remove-all-listeners!))
