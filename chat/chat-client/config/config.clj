(ns config
  (:require [net.cgrand.enlive-html :as html]
            [io.pedestal.app-tools.compile :as compile]))

(def configs
  {:chat-client
   {:build {:watch-files (compile/html-files-in "app/templates")
            ;; When an HTML file changes, trigger the compilation of
            ;; any files which use macros to read in templates. 
            :triggers {:html [#"chat_client/web/app.js"]}}
    :application {:generated-javascript "generated-js"
                  :api-server {:host "localhost" :port 8080 :log-fn nil}
                  :default-template "application.html"
                  :output-root :public}
    :control-panel {:design {:uri "/design.html"
                             :name "Design"
                             :order 0}}
    :built-in {:render {:menu-template "tooling.html"
                        :dir "chat"
                        :renderer 'chat_client.web.rendering
                        :logging? true
                        :order 2}}
    :aspects {:data-ui {:uri "/chat-client-data-ui.html"
                        :params "renderer=auto"
                        :name "Data UI"
                        :order 1
                        :out-file "chat-client-dev-ui.js"
                        :main 'chat_client.web.simulated.start
                        :recording? true
                        :logging? true
                        :template "tooling.html"
                        :output-root :tools-public}
              :ui {:uri "/chat-client-dev-ui.html"
                   :name "UI"
                   :order 3
                   :out-file "chat-client-dev-ui.js"
                   :main 'chat_client.web.simulated.start
                   :logging? true
                   :output-root :tools-public}
              :development {:uri "/chat-client-dev.html"
                            :use-api-server? true
                            :name "Development"
                            :out-file "chat-client-dev.js"
                            :main 'chat_client.web.app
                            :order 4}
              :fresh {:uri "/fresh.html"
                      :name "Fresh"
                      :out-file "fresh.js"
                      :main 'io.pedestal.app.net.repl_client
                      :order 5
                      :output-root :tools-public}
              :production {:uri "/chat-client.html"
                           :use-api-server? true
                           :name "Production"
                           :optimizations :advanced
                           :out-file "chat-client.js"
                           :main 'chat_client.web.app
                           :order 6}}}})
