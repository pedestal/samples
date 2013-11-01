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

;; The configuration below determines how an applicaiton is built,
;; what is built and what features are available in the application
;; development tool.

(def configs
  ;; One project can host multiple applications. The top-level of the
  ;; config map contains one entry for each appliction.
  {:helloworld-app
   {;; :build contains parameters which are passed to the build
    :build {;; :watch-files contains a list of files to watch for
            ;; changes. Each file had a tag associated with it, in
            ;; this case :html.
            :watch-files (compile/html-files-in "app/templates")
            ;; When an HTML file changes, trigger the compilation of
            ;; any files which use macros to read in templates. This
            ;; will force recompilation of these files and update
            ;; the templates.
            ;:triggers {:html [#"helloworld_app/rendering.js"]}
            }
    ;; General application level configuration
    :application {;; The directory where all generated JavaScript for
                  ;; this application will be written.
                  :generated-javascript "generated-js"
                  ;; The default template to use when creating host
                  ;; pages for each aspect below. Override this in an
                  ;; aspect by adding a :template key.
                  :default-template "application.html"
                  ;; The root directory in which to put build
                  ;; output. Possible values are :public and
                  ;; :tools-public. Override this value in an aspect
                  ;; with :tools-output. :public maps to out/public
                  ;; and and :tools-public maps to tools/out/public.
                  :output-root :public}
    ;; Add arbitrary links to the control panel
    :control-panel {:design {:uri "/design.html"
                             :name "Design"
                             ;; The order that this item will appear
                             ;; in the context menu.
                             :order 0}}
    ;; Enable built-in features of the application development
    ;; tool. In the example below we enable the rendering view.
    :built-in {:render {;; The directory where rendering scripts
                        ;; are stored
                        :dir "helloworld-app"
                        ;; The namespace which contains the renderer
                        ;; to use. This namespace must have a
                        ;; `render-config` function which returns a
                        ;; render configuration.
                        :renderer 'helloworld_app.start
                        ;; Enable logging of rendering data when in
                        ;; this view.
                        :logging? true
                        :order 2
                        ;; The render menu uses the tooling.html template
                        :menu-template "tooling.html"}}
    ;; Each aspect provides a unique way to view and interact with
    ;; this application.
    :aspects {
              :development {:uri "/helloworld-app-dev.html"
                            :name "Development"
                            :out-file "helloworld-app-dev.js"
                            :main 'helloworld_app.start
                            :logging? true
                            :order 3}
              :fresh {:uri "/fresh.html"
                      :name "Fresh"
                      :out-file "fresh.js"
                      :main 'io.pedestal.app.net.repl_client
                      :order 4
                      :output-root :tools-public
                      :template "tooling.html"}
              :production {:uri "/helloworld-app.html"
                           :name "Production"
                           :optimizations :advanced
                           :out-file "helloworld-app.js"
                           :main 'helloworld_app.start
                           :order 5}}}})
