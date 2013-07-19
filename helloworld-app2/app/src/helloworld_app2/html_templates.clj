(ns helloworld-app2.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro helloworld-app2-templates
  []
  {:helloworld-app2-page (dtfn (tnodes "helloworld-app2.html" "hello") #{:id})})
