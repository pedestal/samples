(ns todo-mvc.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro todo-mvc-templates
  []
  {:todo-mvc-page (dtfn (tnodes "todo-mvc.html" "todoapp" [[:#todo-list] [:#footer]]) #{})
   :todo-item (dtfn (tnodes "todo-mvc.html" "todo-item") #{:id})
   :count (dtfn (tnodes "todo-mvc.html" "count") #{})
   :filters (dtfn (tnodes "todo-mvc.html" "filters") #{})
   :clear-completed (dtfn (tnodes "todo-mvc.html" "clear-completed") #{})})

;; Note: this file will not be reloaded automatically when it is changed.
