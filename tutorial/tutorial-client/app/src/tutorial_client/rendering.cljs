(ns tutorial-client.rendering
  (:require [domina :as dom]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers :as h]
            [io.pedestal.app.render.events :as events])
  (:require-macros [tutorial-client.html-templates :as html-templates]))

(def templates (html-templates/tutorial-client-templates))

(defn render-template [template-name initial-value-fn]
  (fn [renderer [_ path :as delta] input-queue]
    (let [parent (render/get-parent-id renderer path)
          id (render/new-id! renderer path)
          html (templates/add-template renderer path (template-name templates))]
      (dom/append! (dom/by-id parent) (html (assoc (initial-value-fn delta) :id id))))))

(defn render-value [template key]
  (let [apply-fn (if (= template :self) templates/update-t templates/update-parent-t)]
    (fn [renderer [_ path _ new-value] input-queue]
      (apply-fn renderer path {key (str new-value)}))))

(defn render-other-counters-element [renderer [_ path] input-queue]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path "other-counters")]))

(defn render-config []
  [[:node-create [:tutorial] (render-template :tutorial-client-page
                                              (constantly {:my-counter "0"}))]
   [:node-destroy [:tutorial] h/default-destroy]
   [:transform-enable [:tutorial] (h/add-send-on-click "inc-button")]
   [:transform-disable [:tutorial] (h/remove-send-on-click "inc-button")]
   [:value [:tutorial :my-counter] (render-value :parent :my-counter)]
   [:value [:tutorial :avg] (render-value :parent :avg)]

   [:node-create [:tutorial :other-counters] render-other-counters-element]
   [:node-create [:tutorial :other-counters :*]
    (render-template :other-counter
                     (fn [[_ path]] {:counter-id (last path)}))]
   [:value [:tutorial :other-counters :*] (render-value :self :count)]])
