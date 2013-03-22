(ns todo-mvc.rendering
  (:require [domina :as dom]
            [domina.xpath :as dom-xpath]
            [domina.events :as dom-events]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as t]
            [io.pedestal.app.render.push.handlers.automatic :as d]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.log :as log])
  (:require-macros [todo-mvc.html-templates :as html-templates]))

(def templates (html-templates/todo-mvc-templates))

(defn render-simple-page [renderer [_ path _] transmitter]
  (let [parent (render/get-id renderer path)
        html (t/add-template renderer path (:todo-mvc-page templates))]
    (dom/append! (dom/by-id parent) (html {}))))

(def ^:private enter-key 13)
(declare add-todo-handler)
(defn enable-todo-transforms [renderer [_ path transform msgs] transmitter]
  (condp = transform
    :add-todo (dom-events/listen! (dom/by-id "new-todo")
                                  :keyup
                                  (add-todo-handler transform msgs transmitter))
    (log/error :in :enable-todo-transforms :unmatched transform)))

(defn add-todo-handler [transform-name original-messages transmitter]
  (fn [e]
    (if (= (:keyCode e) enter-key)
      (let [content (-> "new-todo"
                        dom/by-id
                        dom/value)
            messages (msg/fill transform-name original-messages {:content content})]
        (doseq [msg messages]
          (p/put-message transmitter msg))))))

(defn create-todo-item [renderer [event path old new] transmitter]
  ;; At the moment this ID attachs to the first child of our todo li,
  ;; on account of a limitation whereby you cannot set both another
  ;; field AND id on a template.
  (when new
    (let [id (render/new-id! renderer path)
          html (t/add-template renderer path (:todo-item templates))
          todo-map (assoc new :id id)]
      (dom/append! (dom/by-id "todo-list") (html todo-map))
      (if (:completed? todo-map)
        (let [checkbox-sel (str "//*[@id='" id "']//input[@type='checkbox']")
              checkbox (dom-xpath/xpath checkbox-sel)]
          (dom/set-attr! checkbox "checked" "checked"))))))

(def event-to-class
  {:toggle-complete "toggle"
   :delete-todo "destroy"})

(defn create-todo-item-event [renderer [_ path event msgs] transmitter]
  (let [id (render/get-id renderer path)]
    (doseq [msg msgs]
      (let [toggle-selector (str "//*[@id='" id "']//*[@class='" (event-to-class event) "']")]
        (dom-events/listen! (dom-xpath/xpath toggle-selector)
                            :click
                            (fn [_] (p/put-message transmitter msg)))))))

(defn destroy-todo-item [renderer [event path] transmitter]
  (let [view-div-id (render/get-id renderer path)
        view-div (dom/by-id view-div-id)
        ;; We have to remove the parent because id is attached to first
        ;; child of todo item
        parent (.-parentNode view-div)]
    (dom/destroy! parent)))

(defn create-count [r [_ path _]]
  (let [html (t/add-template r path (:count templates))]
    (dom/prepend! (dom/by-id "footer") (html {:number 0 :text "items"}))))

(defn create-filter [r [_ path _]]
  (let [html (t/add-template r path (:filters templates))]
    (dom/prepend! (dom/by-id "footer") (html {:active "all"})))) ;; TODO: implement view for active

(defn update-count [r [event path _ new]]
  (let [key (last path)
        update-map (hash-map key (str new))]
    (t/update-parent-t r path update-map)))

(defn render-config []
  [[:node-create      [] render-simple-page]
   ;; TODO: Split create-todo-item into node-create and value fns
   [:transform-enable [:todo] enable-todo-transforms]
   [:value            [:todo :*] create-todo-item]
   [:transform-enable [:todo :*] create-todo-item-event]
   [:node-destroy     [:todo :*] destroy-todo-item]
   [:node-create      [:count] create-count]
   [:value            [:count :*] update-count]
   [:node-create      [:filters] create-filter]])
