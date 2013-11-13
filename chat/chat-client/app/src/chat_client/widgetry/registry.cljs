(ns chat-client.widgetry.registry
  (:use [cljs.core.async :only [put! close!]]))

(def registry (atom {}))

(defn set-router! [rid rchan ichan]
  (swap! registry assoc :router {:rid rid :rchan rchan :ichan ichan}))

(defn add-widget! [widget]
  (let [{:keys [rid rchan ichan]} (:router @registry)
        {:keys [wid tchan]} widget]
    (swap! registry update-in [:widgets] assoc wid widget)
    (put! rchan [[rid :add [tchan wid :*]]])
    (put! ichan [[[:registry] :created-widget wid]])))

(defn remove-widget! [wid ichan]
  (let [widget (get-in @registry [:widgets wid])
        {:keys [rid rchan ichan]} (:router @registry)]
    (when widget
      (close! (:tchan widget))
      ((:destroy widget))
      (swap! registry update-in [:widgets] dissoc wid)
      (put! rchan [[rid :remove [(:tchan widget) wid :*]]])
      (put! ichan [[[:registry] :removed-widget wid]]))))

(defn remove-all! [ichan exclude]
  (doseq [wid (remove exclude (keys (get @registry :widgets)))]
    (remove-widget! wid ichan)))
