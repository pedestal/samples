(defproject square-root-example "0.0.5-SNAPSHOT"
  :description "Use Heron's method to calculate square roots"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [io.pedestal/pedestal.app "0.0.9-SNAPSHOT"]]
  :aliases {"dumbrepl" ["trampoline" "run" "-m" "clojure.main/main"]})
