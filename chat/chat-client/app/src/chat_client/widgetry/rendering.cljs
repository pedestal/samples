(ns chat-client.widgetry.rendering
  (:require [dommy.core :as dommy]
            [chat-client.widgetry.log :as l])
  (:require-macros [dommy.macros :refer [node sel sel1]]))

(def listeners (atom {}))

(defn add-listener! [type parent-selector selector f]
  (let [callback (fn [evt]
                   (.preventDefault evt)
                   (.stopPropagation evt)
                   (f evt))]
    (swap! listeners assoc-in [selector :listener] {:cb callback :type type})
    (swap! listeners update-in [parent-selector :children] (fnil conj #{}) selector)
    (dommy/listen! (sel1 selector) type callback)))

(defn remove-listener! [selector]
  (let [{:keys [cb type]} (get-in @listeners [selector :listener])]
    (l/log "  " :remove-listener :i selector)
    (when cb
      (dommy/unlisten! (sel1 selector) type cb))
    (swap! listeners dissoc selector)))

(defn remove-all-listeners! [selector]
  (let [children (get-in @listeners [selector :children])]
    (doseq [s (conj children selector)]
      (remove-listener! s))))

(defn clear-all! [selector]
  (remove-all-listeners! selector)
  (dommy/clear! (sel1 selector)))

(defn remove-all! [selector]
  (remove-all-listeners! selector)
  (try
    (dommy/remove! (sel1 selector))
    (catch js/Object e
      (.log js/console (str "Warning! " (pr-str selector) " has already been removed.")))))

(defn gen-id []
  (keyword (str "#" (gensym))))

(defn id-string [id]
  (subs (name id) 1))
