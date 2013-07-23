(ns chat-client.hook)

(defn- wrap-transform [log-fn]
  (fn [[op path f]]
    [op path (log-fn (format "Transform: Matches type and path - %s %s. Received arguments: "
                             op path)
                     f)]))

(defn- wrap-derive [log-fn]
  (fn [[input output f & args]]
    (vec (concat [input output (log-fn (format "Derive: Matches input and output path - %s %s. Received arguments: "
                                               input
                                               output)
                                       f)] 
                 args))))

(defn log-app [app-var log-fn]
  (alter-var-root app-var (fn [app]
                      (assoc app
                             :transform (mapv (wrap-transform log-fn) (:transform app))
                             :derive (mapv (wrap-derive log-fn) (:derive app))))))
