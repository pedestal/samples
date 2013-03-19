; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(defproject square-root-example "0.1.0-SNAPSHOT"
  :description "Use Heron's method to calculate square roots"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [io.pedestal/pedestal.app "0.1.0"]]
  :aliases {"dumbrepl" ["trampoline" "run" "-m" "clojure.main/main"]})
