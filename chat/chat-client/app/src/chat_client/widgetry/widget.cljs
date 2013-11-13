(ns chat-client.widgetry.widget
  (:require [chat-client.widgetry.log :as l])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:use [cljs.core.async :only [chan <!]]))

(defn default-transform! [context state transformation]
  (.log js/console "unknown transformation")
  (.log js/console (pr-str transformation))
  (.log js/console "received by")
  (.log js/console (pr-str context)))

(defn start! [context init-state transform!]
  (let [tchan (chan 10)]
    (go (loop [state init-state]
          (let [transform-message (<! tchan)]
            (l/log "->" (pr-str (:wid context)) :t transform-message)
            (when transform-message
              (recur (reduce (fn [s transformation]
                               (if-let [new-state (transform! context s transformation)]
                                 new-state
                                 s))
                             state
                             transform-message))))))
    tchan))
