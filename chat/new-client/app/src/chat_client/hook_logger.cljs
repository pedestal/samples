(ns chat-client.hook-logger)

(defn log-fn [msg f]
  (fn [& args]
    (.log js/console msg (clj->js args))
    (apply f args)))
