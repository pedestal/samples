(defproject template-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [io.pedestal/pedestal.service "0.0.1-SNAPSHOT"]
                 [io.pedestal/pedestal.jetty "0.0.1-SNAPSHOT"]
                 [ch.qos.logback/logback-classic "1.0.7"]
                 [org.slf4j/jul-to-slf4j "1.7.2"]
                 [org.slf4j/jcl-over-slf4j "1.7.2"]
                 [org.slf4j/log4j-over-slf4j "1.7.2"]
                 [hiccup "1.0.2"]
                 [enlive "1.0.1"]
                 [comb "0.1.0"]
                 [org.antlr/stringtemplate "4.0.2"]
                 [de.ubercode.clostache/clostache "1.3.1"]]
  :profiles {:dev {:source-paths ["dev"]}}
  :resource-paths ["config" "resources"]
  :main ^{:skip-aot true} template-server.server)
