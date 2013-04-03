(ns todo-mvc.services)

;; The services namespace responsible for communicating with back-end
;; services. It receives messages from the application's behavior,
;; makes requests to services and sends responses back to the
;; behavior.
;;
;; This namespace will usually contain a function which can be
;; configured to receive effect events from the bahavior in the file
;;
;; app/src/todo_mvc/start.cljs
;;
;; After creating a new application, set the effect handler function
;; to recieve effect
;;
;; (app/consume-effect app services-fn)
;;
;; A very simple example of a services function which echos all events
;; back to the behavior is shown below

(comment

  ;; The services implementation will need some way to send messages
  ;; back to the application. The queue passed to services function
  ;; will convey messages to the application.
  (defn echo-services-fn [message queue]
    (put-message queue message))
  
  )

;; During development, it is helpful to implement services which
;; simulate communication with the real services. This implementaiton
;; can be placed in the file
;;
;; app/src/todo_mvc/simulated/services.cljs
;;
