(ns greeting.html-templates
  (:use [io.pedestal.app.templates :only [render tfn dtfn tnodes template-children]])
  (:require [net.cgrand.enlive-html :as html]))

(defmacro greeting-templates
  []
  {:greeting-page (dtfn (tnodes "greeting.html" "greeting") #{})})
