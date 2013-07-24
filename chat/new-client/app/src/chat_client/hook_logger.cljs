(ns chat-client.hook-logger)

(defn log-fn [msg f]
  (fn [& args]
    (.log js/console msg (pr-str args))
    (apply f args)))
