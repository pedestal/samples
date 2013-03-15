; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

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
