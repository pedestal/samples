(ns chat-client.hook)

(defn- wrap-transform [log-fn]
  (fn [[op path f]]
    [op path (log-fn (format "Transform: Matches %s %s. Received arguments: "
                             op path)
                     f)]))

(defn- wrap-derive [log-fn]
  (fn [[input output f & args]]
    (vec (concat [input output (log-fn (format "Derive: Matches %s %s. Received arguments: "
                                               input
                                               output)
                                       f)] 
                 args))))

(defn- wrap-effect [log-fn]
  (fn [[paths f & args]]
    (vec (concat [paths (log-fn (format "Effect: Matches %s. Received arguments: " paths) f)]
                 args))))

(defn- wrap-emit [log-fn]
  (fn [emit-entry]
    (if (map? emit-entry)
      {:init (log-fn (format "Emit: Matches %s. Received arguments: " :init) (:init emit-entry))}
      (if (vector? emit-entry)
        (let [[paths f] emit-entry]
          [paths (log-fn (format "Emit: Matches %s. Received arguments: " paths) f)])
        (prn "EMIT not handled" emit-entry)))))

(defn log-app [app-var log-fn]
  (alter-var-root app-var (fn [app]
                      (assoc app
                             :transform (mapv (wrap-transform log-fn) (:transform app))
                             :derive (set (map (wrap-derive log-fn) (:derive app)))
                             :effect (set (map (wrap-effect log-fn) (:effect app)))
                             :emit (mapv (wrap-emit log-fn) (:emit app))))))
