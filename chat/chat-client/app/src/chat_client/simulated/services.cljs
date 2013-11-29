(ns chat-client.simulated.services
  (:require [io.pedestal.app.match :as match]
            [chat-client.widgetry.log :as l]
            [chat-client.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:use [cljs.core.async :only [chan <! >! put! alts! timeout close!]]))

(defn start-receiving
  "This is used for testing without a server-side component; it simulates the server by sending new
  messages to the application every 10 seconds."
  [[id _ _ :as inform] ichan]
  (put! ichan
        [[id
          :inbound-received
          {:text (str "incoming message " (gensym))
           :nickname (str (gensym))
           :id (util/random-id)}]])
  (.setTimeout js/window
               (fn [] (start-receiving inform ichan))
               10000))

(defn receive-inbound [[id _ msg] ichan]
  (js/setTimeout #(put! ichan [[id :inbound-received msg]]) 500)
  (.log js/console (str "Send to Server: " (pr-str msg))))

(def config
  (match/index [[receive-inbound [:services :message] :receive-inbound]
                [start-receiving [:services :message] :start-receiving]]))

(defn start-services! [ichan]
  (let [tchan (chan 10)]
    (go (while true
          (let [transform (<! tchan)]
            (l/log "->" :transform-services :t transform)
            (doseq [transformation transform]
              (when-let [handler-fn (ffirst (match/match-items config transformation))]
                (handler-fn transformation ichan))))))
    tchan))
