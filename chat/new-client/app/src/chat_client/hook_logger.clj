(ns chat-client.hook-logger)

(defn log-fn [msg f]
  (fn [& args]
    (print (str msg " "))
    (prn args)
    (apply f args)))
