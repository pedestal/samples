(ns helloworld-app.html-templates
  (:use [io.pedestal.app.templates :only [render tfn dtfn tnodes template-children]])
  (:require [net.cgrand.enlive-html :as html]))

(defmacro helloworld-app-templates
  []
  {:helloworld-app-page (dtfn (tnodes "helloworld-app.html" "word-transform") #{})})
