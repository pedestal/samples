(ns chat-client.simulated.start
  (:require [io.pedestal.app.construct :as construct]
            [io.pedestal.app.route :as route]
            [io.pedestal.app.util.observers :as observers]
            [chat-client.widgetry.root :as root]
            [chat-client.widgetry.registry :as registry]
            [chat-client.app :as app]
            [chat-client.simulated.services :as services]
            [chat-client.widgets.chat :as wchat])
  (:use [cljs.core.async :only [put! chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def widgets
  {:chat wchat/create!})

(defn hide-functions [transform-message]
  (mapv (fn [msg]
          (mapv #(if (fn? %) :f %) msg))
        transform-message))

(defn log-print [message]
  (cond (= (:in message) :router)
        (.log js/console (str "> " (pr-str :router
                                           (:id message)
                                           (hide-functions (:transform message)))))
        
        :else (.log js/console (pr-str message))))

(defn create-app [start-services!]
  (let [cin (construct/build {:info {:inbound {:received []}
                                     :outbound {:sent []}}} app/config)
        services-transform (start-services! cin)
        widgets-transform-c (chan 10)]
    
    (route/router [:ui :router] widgets-transform-c)
    (observers/subscribe :log log-print)
    (registry/set-router! [:ui :router] widgets-transform-c cin)
    
    (put! cin [[[:io.pedestal.app.construct/router] :channel-added
                services-transform [:services :* :**]]])
    
    (put! cin [[[:io.pedestal.app.construct/router] :channel-added
                widgets-transform-c [:ui :* :**]]])
    
    (let [root-widget (root/create! [:ui :root] :#content cin :widgets widgets)]
      (registry/add-widget! root-widget))
    
    (put! cin [[[:app] :startup]])
    cin))

(defn ^:export main []
  (create-app services/start-services!))
