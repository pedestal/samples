; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns helloworld-app.start
  (:require [io.pedestal.app.construct :as construct]
            [io.pedestal.app.route :as route]
            ;; This needs to be included somewhere in order for the tools to work.
            [io.pedestal.app-tools.tooling :as tooling]
            [domina :as dom])
  (:use [cljs.core.async :only [put! chan]]))

(defn render-value
  [[[_ _ _ model]]]
  (dom/destroy-children! (dom/by-id "content"))
  (dom/append! (dom/by-id "content")
               (str "<h1>" (get-in model [:info :count]) " Hello Worlds</h1>"))
  [])

(defn receive-input [cin]
  (put! cin [[[:app] :inc]])
  (.setTimeout js/window #(receive-input cin) 3000))

(defn inc-counter [inform]
  [[[[:info :count] inc]]])

(defn inspect [s]
  (fn [inform-message]
    (.log js/console s (pr-str inform-message))
    []))

(def config
  {:in [[inc-counter [:app] :inc]
        [(inspect "Input:") [:**] :*]]
   :out [[render-value [:info :count] :*]
         [(inspect "Rendering:") [:**] :*]]})

(defn create-app []
  (let [cin (construct/build {:info {:count 0}} config)]
    (receive-input cin)
    cin))

(defn ^:export main []
  (create-app))
