(ns ^:shared todo-mvc.behavior
    (:require [clojure.string :as string]
              [clojure.set :as set]
              [io.pedestal.app.util.log :as log]
              [io.pedestal.app.util.platform :as platform]
              [io.pedestal.app.messages :as msg]))

;; Model =======================================================================
(defmulti process-todo-message (fn [_ event] (msg/type event)))

(defn new-todo [content]
  {:content (clojure.string/trim content)
   :created (platform/date)
   :status "uncompleted"                ; TODO: this is _really_ just
                                        ; the css class, not actual status
   :completed? false
   :uuid (gensym "TODO_")})

(defmethod process-todo-message :add-todo [state event]
  (let [new-todo (new-todo (:content event))]
    (if (seq (:content new-todo))
      (assoc state (:uuid new-todo) new-todo)
      state)))

(defmethod process-todo-message :delete-todo [state event]
  (dissoc state (:uuid event)))

(defmethod process-todo-message :clear-completed [state event]
  (into {} (remove (fn [[_ {status :status}]] (= "completed" status)) state)))

(defmethod process-todo-message :set-content [state event]
  (update-in state [(:uuid event) :content] (constantly (:content event))))

(def toggle-complete
  {"completed" "uncompleted"
   "uncompleted" "completed"
   nil "uncompleted"})

(defmethod process-todo-message :toggle-complete [state event]
  (-> state
      (update-in [(:uuid event) :status] toggle-complete)
      (update-in [(:uuid event) :completed?] not )))

(defmethod process-todo-message :complete-all [state event]
  (into {} (map (fn [[id todo]] [id (assoc todo :completed (platform/date))]) state)))

(def todo-messages
  #{:add-todo
    :delete-todo
    :clear-completed
    :set-content
    :toggle-complete
    :toggle-all})

(defn todo-model [state event]
  (cond
   (= (msg/type event) msg/init) (:value event)
   (contains? todo-messages (msg/type event)) (process-todo-message state event)
   :else (do (log/debug :in :model/todo
                        :message "Received unmatched event"
                        :event (msg/type event))
             state)))


;; Combines =======================================================================

(defn completed [state input old new]
  (filter (fn [[_ {current-status :status}]] (= current-status "completed")) new))

(defn active [_ _ _ new]
  (filter (fn [[_ {current-status :status}]] (= current-status "uncompleted")) new)) 

(defn count-todos [state input old new]
  (count new))

(defn items-left-pluralized [_ _ _ new]
  {:number new, :text (if (= 1 new) "item" "items")})

(defn everything-completed? [_ inputs]
  (let [completed-count (-> inputs :completed-count :new)
        active-count (-> inputs :active-count :new)]
    (and (= active-count 0)
         (> completed-count 0))))

;; Emitters ======================================================================

(defmulti emit-dispatch (fn [_ input] input))

(defmethod emit-dispatch :default [input changed-input]
  (log/debug :in :emit-dispatch :message "hit default for input" :changed changed-input)
  [])

(defmethod emit-dispatch :items-left [input changed-input]
  [[:value [:count :number] (get-in input [changed-input :new :number])]
   [:value [:count :text]   (get-in input [changed-input :new :text])]])

(defmethod emit-dispatch :completed-count [input changed-input]
  (let [count (changed-input input)]
    (cond
     (< (:new count) 1)                          [[:transform-disable [:todo] :clear-completed]]
     (and (> (:new count) 0) (> (:old count) 0)) []
     (and (> (:new count) 0) (< (:old count) 1)) [[:transform-enable [:todo] :clear-completed [{msg/topic :todo}]]])))

(defmethod emit-dispatch :everything-completed? [input changed-input]
  (let [value (-> input changed-input :new)]
    [[:attr [:todo] :everything-completed? value]]))

(defmethod emit-dispatch :todo [input changed-input]
  (let [old-state (-> input changed-input :old)
        new-state (-> input changed-input :new)
        removed (set/difference (set old-state) (set new-state))
        added (set/difference (set new-state) (set old-state))]
    (log/debug :in :emit-dispatch :dispatch :todo :added added :removed removed)
    (concat
     (mapcat (fn [[uuid _]] [[:node-destroy [:todo uuid]]])
             removed)
     (map (fn [[uuid todo]]
            {:todo {uuid {:value todo
                          :transforms {:toggle-complete [{msg/topic :todo msg/type :toggle-complete :uuid uuid}]
                                       :delete-todo     [{msg/topic :todo msg/type :delete-todo :uuid uuid}]}}}})
          added))))

;; Events
;; 1. Clicking the checkbox marks the todo as complete by updating it's completed value and toggling the class completed on it's parent <li>
;; 2. Double-clicking the <label> activates editing mode, by toggling the .editing class on it's <li>
;; 3. Hovering over the todo shows the remove button (.destroy)

(def initial-tree
  [{:todo {:transforms {:add-todo [{msg/topic :todo (msg/param :content) {}}]}}
    :count {:number {}
            :text {}}
    :filters {}}])

(defn treeify
  ([inputs] initial-tree)
  ;; //*[@id='clear-completed']
  ([inputs changed-inputs]
;;     (log/debug :in :emit :args {:inputs inputs :changed-inputs changed-inputs})
     (mapcat (fn [changed-input]
               (emit-dispatch inputs changed-input))
             changed-inputs)))

;; App Dataflow
(def todo-app
  {:transform  {:todo         {:init {} :fn todo-model}}
   :combine    {:completed    {:input #{:todo} :fn completed}
                :completed-count {:input #{:completed} :fn count-todos}
                :active       {:input #{:todo} :fn active}
                :active-count {:input #{:active} :fn count-todos}
                :items-left   {:input #{:active-count} :fn items-left-pluralized}
                :everything-completed? {:input #{:active-count :completed-count} :fn everything-completed?}}
   :emit       {:all          {:input #{:everything-completed? :items-left :todo :completed-count} :fn treeify}}})
