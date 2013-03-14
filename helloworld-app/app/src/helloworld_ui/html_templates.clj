(ns helloworld-ui.html-templates
  (:use [io.pedestal.app.templates :only [render tfn dtfn tnodes template-children]])
  (:require [net.cgrand.enlive-html :as html]))

(defmacro helloworld-ui-templates
  []
  {:helloworld-ui-page (dtfn (tnodes "helloworld-ui.html" "word-transform") #{})})
