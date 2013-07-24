(ns ^:shared chat-client.hook
  (:require [clojure.string]))

(defn- log-string [state & args]
  (format "DATAFLOW: %s matched on \"%s\" and received arguments:"
          state (clojure.string/join " " args)))

(defn- wrap-transform [log-fn]
  (fn [[op path f]]
    [op path (log-fn (log-string "TRANSFORM" op path) f)]))

(defn- wrap-derive [log-fn]
  (fn [[input output f & args]]
    (vec (concat [input output (log-fn (log-string "DERIVE" input output) f)]
                 args))))

(defn- wrap-effect [log-fn]
  (fn [[paths f & args]]
    (vec (concat [paths (log-fn (log-string "EFFECT" paths) f)]
                 args))))

(defn- wrap-emit [log-fn]
  (fn [emit-entry]
    (if (map? emit-entry)
      {:init (log-fn (log-string "EMIT" :init) (:init emit-entry))}
      (if (vector? emit-entry)
        (let [[paths f] emit-entry]
          [paths (log-fn (log-string "EMIT" paths) f)])
        ;; TODO: Remove once emit has been reviewed. Yes this fails in cljs
        (prn "EMIT not handled" emit-entry)))))

(defn wrap-app [app log-fn]
  (assoc app
         :transform (mapv (wrap-transform log-fn) (:transform app))
         :derive (set (map (wrap-derive log-fn) (:derive app)))
         :effect (set (map (wrap-effect log-fn) (:effect app)))
         :emit (mapv (wrap-emit log-fn) (:emit app))))
