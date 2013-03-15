; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns greeting.app
  (:require [greeting.behavior :as behavior]
            [greeting.rendering :as rendering]
            [greeting.services :as services]
            [io.pedestal.app :as app]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.net.repl-client :as repl-client]))

;; Start

(defn ^:export main []
  (let [app (app/build behavior/greeting-app)
        render-fn (push-render/renderer "content" rendering/render-config render/log-fn)]
    (services/receive-messages app)
    (render/consume-app-model app render-fn)
    (app/begin app)))
