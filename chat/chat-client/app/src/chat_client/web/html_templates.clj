(ns chat-client.web.html-templates
  (:use [io.pedestal.app.templates :only [render tfn dtfn tnodes template-children]])
  (:require [net.cgrand.enlive-html :as html]))

(defmacro sample-templates
  []
  {:message (dtfn (tnodes "chat-client.html" "message") #{:id})
   :chat (dtfn (tnodes "chat-client.html" "chat") #{})})
