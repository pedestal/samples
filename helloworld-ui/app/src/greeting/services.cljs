; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns greeting.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]))

(defn receive-messages [app]
  (.setTimeout js/window
               (fn [] (p/put-message (:input app)
                                    {msg/topic :greeting-transform
                                     msg/type :something
                                     :input "Have a good one."})) 5000))
