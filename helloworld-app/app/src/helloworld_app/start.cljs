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
            [domina :as dom])
  (:use [cljs.core.async :only [put! chan]]))

(defn render-value
  [_ [[_ _ _ model]]]
  (dom/destroy-children! (dom/by-id "content"))
  (dom/append! (dom/by-id "content")
               (str "<h1>" (get-in model [:info :count]) " Hello Worlds</h1>"))
  [])

(defn receive-input [cin]
  (put! cin [[[:app] :inc]])
  (.setTimeout js/window #(receive-input cin) 3000))

(defn inc-counter [_ inform]
  [[[[:info :count] inc]]])

(def config
  {:in [[inc-counter [:app] :inc]]
   :out [[render-value [:info :count] :*]]})

(defn create-app []
  (let [cin (construct/build {:info {:count 0}} config)]
    (receive-input cin)
    cin))

(defn ^:export main []
  (create-app))
