(ns chat-client.web.rendering
  (:require [domina :as dom]
            [domina.events :as dom-events]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.events :as events]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.render.push.handlers.automatic :as auto])
  (:require-macros [chat-client.web.html-templates :as html-templates]))

(def templates (html-templates/sample-templates))

(defn render-chat-page [r [_ path] d]
  (let [parent (render/get-parent-id r path)
        html (templates/add-template r path (:chat templates))]
    (dom/append! (dom/by-id parent) (html {:title "Chat"}))))

(defn destroy-chat-page [r [_ path] d]
  (let [parent (render/get-parent-id r path)]
    (dom/destroy-children! (dom/by-id parent))))

(defn form-transform-enable [r [_ path transform-name messages] d]
  (condp = transform-name
    :send-message
    ;; Remove class hide from .enter-message
    (let [enter-message-form (dom/by-class "enter-message")]
      (dom/remove-class! enter-message-form "hide")
      (.focus (dom/by-id "message-input"))
      (events/send-on :submit
                      enter-message-form
                      d
                      (fn [] (let [text-node (dom/by-id "message-input")
                                  text (.-value text-node)]
                              (set! (.-value text-node) "")
                              (msg/fill transform-name messages {:text text})))))

    :clear-messages
    (events/send-on-click (dom/by-id "clear-button") d transform-name messages)

    :set-nickname
    (let [enter-nickname-form (dom/by-class "enter-nickname")]
      (dom/add-class! (dom/by-id "root") "startup")
      (dom/remove-class! enter-nickname-form "hide")
      (.focus (dom/by-id "nickname-input"))
      (events/send-on :submit
                      enter-nickname-form
                      d
                      (fn []
                        (let [nickname-node (dom/by-id "nickname-input")
                              nickname (.-value nickname-node)]
                          (set! (.-value nickname-node) "")
                          (msg/fill transform-name messages {:nickname nickname})))))

    :clear-nickname
    (events/send-on-click (dom/by-class "nickname-icon")
                          d
                          (msg/fill transform-name messages))))

(defn form-transform-disable [r [_ path transform-name messages] d]
  (condp = transform-name
    :send-message
    (let [enter-message-form (dom/by-class "enter-message")]
      (dom/add-class! enter-message-form "hide")
      (dom-events/unlisten! enter-message-form))

    :set-nickname
    (let [enter-nickname-form (dom/by-class "enter-nickname")]
      (dom/remove-class! (dom/by-id "root") "startup")
      (dom/add-class! enter-nickname-form "hide")
      (dom-events/unlisten! enter-nickname-form))

    :clear-nickname
    (dom-events/unlisten! (dom/by-class "nickname-icon"))))

(defn- format-time [d]
  (let [pad (fn [n] (if (< n 10) (str "0" n) (str n)))]
    (str (pad (.getHours d)) ":"
         (pad (.getMinutes d)) ":"
         (pad (.getSeconds d)))))

(defn create-message-node [r [_ path] d]
  (let [id (render/new-id! r path)
        html (templates/add-template r path (:message templates))]
    (templates/prepend-t r [:chat] {:messages (html {:id id :status "pending"})})))

(defn update-message [r [_ path o n] d]
  (let [id (render/get-id r path)
        msg (assoc n :id (:id n) :time (format-time (:time n)))
        msg (if (:status n) (update-in msg [:status] name) msg)]
    (templates/update-t r path msg)))

(defn render-config []
  [[:node-create       [:chat]         render-chat-page]
   [:node-destroy      [:chat]         destroy-chat-page]
   [:transform-enable  [:chat :form]   form-transform-enable]
   [:transform-disable [:chat :form]   form-transform-disable]
   [:node-create       [:chat :log :*] create-message-node]
   [:node-destroy      [:chat :log :*] auto/default-exit]
   [:value             [:chat :log :*] update-message]])
