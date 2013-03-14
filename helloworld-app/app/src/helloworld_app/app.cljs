(ns helloworld-app.app
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            [domina :as dom]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.net.repl-client :as repl-client]
            [io.pedestal.app-tools.tooling :as tooling])
  (:require-macros [helloworld-app.html-templates :as html-templates]))

(comment

  (def application-state (atom 0))
  
  (defn render [old-state new-state]
    (dom/destroy-children! (dom/by-id "content"))
    (dom/append! (dom/by-id "content")
                 (str "<h1>" new-state " Hello Worlds</h1>")))

  (add-watch application-state :app-watcher
             (fn [key reference old-state new-state]
               (render old-state new-state)))

  (defn receive-input []
    (swap! application-state inc)
    (.setTimeout js/window #(receive-input) 3000))
  
  (defn ^:export main []
    (receive-input))
  )


(comment

  (defn count-model [old-state message]
    (condp = (msg/type message)
      msg/init (:value message)
      :inc (inc old-state)))

  (defmulti render (fn [& args] (first args)))

  (defmethod render :default [_]
    nil)

  (defmethod render :value [_ _ old-value new-value]
    (dom/destroy-children! (dom/by-id "content"))
    (dom/append! (dom/by-id "content")
                 (str "<h1>" new-value " Hello Worlds</h1>")))

  (defn render-fn [deltas input-queue]
    (doseq [d deltas]
      (apply render d)))

  (def count-app {:models {:count {:init 0 :fn count-model}}})

  (defn receive-input [input-queue]
    (p/put-message input-queue {msg/topic :count msg/type :inc})
    (.setTimeout js/window #(receive-input input-queue) 3000))

  (defn ^:export main []
    (let [app (app/build count-app)]
      (render/consume-app-model app render-fn)
      (receive-input (:input app))
      (app/begin app)))

  )

(comment

  (defn count-model [old-state message]
    (condp = (msg/type message)
      msg/init (:value message)
      :inc (inc old-state)))

  (defn render-value [r [_ _ old-value new-value] input-queue]
    (dom/destroy-children! (dom/by-id "content"))
    (dom/append! (dom/by-id "content")
                 (str "<h1>" new-value " Hello Worlds</h1>")))
  
  (def count-app {:models {:count {:init 0 :fn count-model}}})

  (defn receive-input [input-queue]
    (p/put-message input-queue {msg/topic :count msg/type :inc})
    (.setTimeout js/window #(receive-input input-queue) 3000))
  
  (defn ^:export main []
    (let [app (app/build count-app)
          render-fn (push/renderer "content" [[:value [:**] render-value]])]
      (render/consume-app-model app render-fn)
      (receive-input (:input app))
      (app/begin app)))

  )

(def templates (html-templates/hello-world-templates))

(defn count-model [old-state message]
  (condp = (msg/type message)
    msg/init (:value message)
    :inc (inc old-state)))

(defn render-page [renderer [_ path] input-queue]
  (let [parent (push/get-parent-id renderer path)
        html (templates/add-template renderer path (:hello-world-page templates))]
    (dom/append! (dom/by-id parent) (html {:message ""}))))

(defn render-value [renderer [_ path old-value new-value] input-queue]
  (templates/update-t renderer path {:message (str new-value)}))

(def render-config
  [[:node-create [:*] render-page]
   [:value       [:*] render-value]])

(def count-app {:models {:count {:init 0 :fn count-model}}})

(defn receive-input [input-queue]
  (p/put-message input-queue {msg/topic :count msg/type :inc})
  (.setTimeout js/window #(receive-input input-queue) 3000))

(defn ^:export main []
  (let [app (app/build count-app)
        render-fn (push/renderer "content" render-config)]
    (render/consume-app-model app render-fn)
    (receive-input (:input app))
    (app/begin app)))
