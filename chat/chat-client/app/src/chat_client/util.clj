(ns ^:shared chat-client.util
    (:require [io.pedestal.app.util.platform :as platform]))

(defn random-id []
  (str (.getTime (platform/date)) "-" (rand-int 1E6)))
