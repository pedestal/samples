(ns greeting.rendering
  (:require [domina :as dom]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates])
  (:require-macros [greeting.html-templates :as html-templates]))

;; Rendering

(def templates (html-templates/greeting-templates))

(defn greeting-render-fn [r [_ path _ v] d]
  (templates/update-parent-t r path {:greeting v}))

(defn render-simple-page [r [_ path] d]
  (let [parent (render/get-parent-id r path)
        html (templates/add-template r path (:greeting-page templates))]
    (dom/append! (dom/by-id parent) (html {:greeting "Salutations!"}))))

(def render-config
  [[:node-create [:app] render-simple-page]
   [:value [:app :greeting] greeting-render-fn]])
