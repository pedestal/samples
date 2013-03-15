; Copyright 2013 Relevance, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(ns config
  (:require [net.cgrand.enlive-html :as html]
            [io.pedestal.app-tools.compile :as compile]))

(def configs
  {:helloworld-ui
   {:build {:watch-files (compile/html-files-in "templates")
            ;; When an HTML file changes, trigger the compilation of
            ;; any files which use macros to read in templates. 
            :triggers {:html [#"helloworld_ui/app.js" #"greeting/app.js"]}}
    :application {:generated-javascript "generated-js"
                  :api-server {:host "localhost" :port 8080 :log-fn nil}
                  :default-template "application.html"
                  :output-root :public}
    :control-panel {:design {:uri "/design.html"
                             :name "Design"
                             :order 0}}
    :aspects {:development {:uri "/helloworld-ui-dev.html"
                            :name "Development"
                            :out-file "helloworld-ui-dev.js"
                            :scripts ["goog.require('helloworld_ui.app');"
                                      "helloworld_ui.app.main();"]
                            :order 1}
              :fresh {:uri "/fresh.html"
                      :name "Fresh"
                      :out-file "fresh.js"
                      :scripts ["goog.require('io.pedestal.app.net.repl_client');"
                                "io.pedestal.app.net.repl_client.repl();"]
                      :order 2
                      :output-root :tools-public}
              :production {:uri "/helloworld-ui.html"
                           :name "Production"
                           :optimizations :advanced
                           :out-file "helloworld-ui.js"
                           :scripts ["helloworld_ui.app.main();"]
                           :order 3}
              :greeting {:uri "/greeting-dev.html"
                         :name "Greeting"
                         :out-file "greeting-dev.js"
                         :scripts ["goog.require('greeting.app');"
                                   "greeting.app.main();"]
                         :order 4}}}})
