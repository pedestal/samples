; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns chat-client.web.app
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.render.push :as push-render]
            ;; repl-client must be required somewhere in order for the
            ;; fresh view to work.
            [io.pedestal.app.net.repl-client :as repl-client]
            [chat-client.behavior :as behavior]
            [chat-client.web.rendering :as rendering]
            [chat-client.web.services :as services]))

(defn ^:export main []
  (let [app (app/build behavior/chat-client)
        render-fn (push-render/renderer "content" (rendering/render-config) render/log-fn)
        services (services/->Services app)]
    (p/start services)
    (render/consume-app-model app render-fn)
    (app/consume-effects app services/services-fn)
    (app/begin app)
    {:app app}))
