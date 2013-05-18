(ns tutorial-client.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro tutorial-client-templates
  []
  {:tutorial-client-page (dtfn (tnodes "tutorial-client.html" "tutorial" [[:#other-counters]])
                               #{:id})
   :other-counter (dtfn (tnodes "tutorial-client.html" "other-counter") #{:id})})
