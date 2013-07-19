(ns chat-client.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro chat-client-templates
  []
  {:chat-client-page (dtfn (tnodes "chat-client.html" "hello") #{:id})})
