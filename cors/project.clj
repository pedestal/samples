(defproject cors "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [io.pedestal/pedestal.service "0.0.1-SNAPSHOT"]
                 [io.pedestal/pedestal.jetty "0.0.1-SNAPSHOT"]
                 [org.slf4j/jul-to-slf4j "1.7.2"]
                 [org.slf4j/jcl-over-slf4j "1.7.2"]
                 [org.slf4j/log4j-over-slf4j "1.7.2"]
                 [ring-cors/ring-cors "0.1.0"]]
  :profiles {:dev {:source-paths ["dev"]}}
  :resource-paths ["config"]
  :main ^{:skip-aot true} cors.server)
