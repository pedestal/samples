; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(defproject helloworld-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/tools.namespace "0.2.1"]
                 [domina "1.0.1"]
                 [ch.qos.logback/logback-classic "1.0.6"]
                 [org.clojure/clojurescript "0.0-1450"]
                 [io.pedestal/pedestal.app "0.1.2"]
                 [io.pedestal/pedestal.app-tools "0.1.2"]]
  :profiles {:dev {:source-paths ["dev"]}}
  :source-paths ["app/src" "app/templates"]
  :resource-paths ["config"]
  :aliases {"dumbrepl" ["trampoline" "run" "-m" "clojure.main/main"]})
