(ns chat-client.widgetry.log)

(defn- pad [s length]
  (if (>= (count s) length)
    s
    (apply str s (repeat (- length (count s)) " "))))

(defn log [dir type level message & args]
  (let [t (pad (name type) 20)
        l (name level)
        m message]
    (.log js/console (apply str dir " " t " " l " " m " " args))))
