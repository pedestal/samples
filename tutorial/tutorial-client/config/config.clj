(ns config
  (:require [net.cgrand.enlive-html :as html]
            [io.pedestal.app-tools.compile :as compile]))

(def configs
  {:tutorial-client
   {:build {:watch-files (compile/html-files-in "app/templates")
            :triggers {:html [#"tutorial_client/rendering.js"]}}
    :application {:generated-javascript "generated-js"
                  :default-template "application.html"
                  :output-root :public
                  :api-server {:host "localhost" :port 8080 :log-fn nil}}
    :control-panel {:design {:uri "/design.html"
                             :name "Design"
                             :order 0}}
    :built-in {:render {:dir "tutorial-client"
                        :renderer 'tutorial_client.rendering
                        :logging? true
                        :order 2
                        :menu-template "tooling.html"}}
    :aspects {:data-ui {:uri "/tutorial-client-data-ui.html"
                        :name "Data UI"
                        :params "renderer=auto"
                        :order 1
                        :out-file "tutorial-client-data-ui.js"
                        :main 'tutorial_client.simulated.start
                        :recording? true
                        :logging? true
                        :output-root :tools-public
                        :template "tooling.html"}
              :ui {:uri "/tutorial-client-dev-ui.html"
                   :name "UI"
                   :order 2
                   :out-file "tutorial-client-dev-ui.js"
                   :main 'tutorial_client.simulated.start
                   :recording? true
                   :logging? true
                   :output-root :tools-public}
              :development {:uri "/tutorial-client-dev.html"
                            :use-api-server? true
                            :name "Development"
                            :out-file "tutorial-client-dev.js"
                            :main 'tutorial_client.start
                            :logging? true
                            :order 3}
              :fresh {:uri "/fresh.html"
                      :name "Fresh"
                      :out-file "fresh.js"
                      :main 'io.pedestal.app.net.repl_client
                      :order 4
                      :output-root :tools-public
                      :template "tooling.html"}
              :production {:uri "/tutorial-client.html"
                           :use-api-server? true
                           :name "Production"
                           :optimizations :advanced
                           :out-file "tutorial-client.js"
                           :main 'tutorial_client.start
                           :order 5}}}})
