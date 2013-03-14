(ns greeting.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]))

(defn receive-messages [app]
  (.setTimeout js/window
               (fn [] (p/put-message (:input app)
                                    {msg/topic :greeting-transform
                                     msg/type :something
                                     :input "Have a good one."})) 5000))
