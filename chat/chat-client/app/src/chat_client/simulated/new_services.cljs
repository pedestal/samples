(ns chat-client.simulated.new-services
  (:require [io.pedestal.app.match :as match]
            [chat-client.widgetry.log :as l])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:use [cljs.core.async :only [chan <! >! put! alts! timeout close!]]))

(defn receive-inbound [[id _ msg] ichan]
  (js/setTimeout #(put! ichan [[id :inbound-received msg]]) 500)
  (.log js/console (str "Send to Server: " (pr-str msg))))

(def config
  (match/index [[receive-inbound [:services :message] :receive-inbound]]))

(defn start-services! [ichan]
  (let [tchan (chan 10)]
    (go (while true
          (let [transform (<! tchan)]
            (l/log "->" :transform-services :t transform)
            (doseq [transformation transform]
              (when-let [handler-fn (ffirst (match/match-items config transformation))]
                (handler-fn transformation ichan))))))
    tchan))
