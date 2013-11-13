(ns ^:shared chat-client.app)

(defn visible-widgets [_ [[_ event wid]]]
  (cond (= event :created-widget) [[[[:info :visible] (fnil conj #{}) wid]]]
        (= event :removed-widget) [[[[:info :visible] disj wid]]]))

(defn startup [_ inform-message]
  [[[[:ui :root] :change-screen :chat [:ui :chat]]]])

(defn login [_ [[_ _ creds]]]
  [[[[:ui :login] :authenticating (:uid creds)]
    #_[[:services :auth] :authenticate (:uid creds) (:pw creds)]]])

(defn set-nickname [_ [[_ _ val]]]
  [[[[:info :nickname] constantly (:nickname val)]
    ;; TODO - display nickname
    ;; enable clear-nickname
    ;; enable send-message
    ]])

(defn authenticated [_ [[_ _ creds]]]
  [[[[:ui :root] :change-screen :counter [:ui :counter]]
    [[:info :user] assoc :creds creds]]])

(defn inc-button-clicked [_ [[[_ _ counter-id]]]]
  [[[[:info :counter counter-id] inc]]])

(defn text-updated [_ inform-message]
  (vector
   (reduce (fn [a [[_ _ cid] _ _ model]]
             (conj a [[:ui :number cid] :set-text (get-in model [:info :counter cid])]))
           []
           inform-message)))

;; TODO - investigate possible bug: inform-message is being passed as a first arg
(defn inspect [s]
  (fn [path inform-message]
    (.log js/console s)
    (.log js/console (pr-str path inform-message))
    []))

(def config
  {:in [[visible-widgets [:registry] :*]
        [startup [:app] :startup]
        [set-nickname [:ui :set-nickname] :set-nickname]
        #_[authenticated [:services :auth] :authenticated]
        [(inspect "<<<<<<<<") [:**] :*]]
   
   :out [#_[text-updated [:info :counter :*] :*]
         #_[set-nickname [:info :nickname] :added [:info :nickname] :updated] ; -> [:ui :chat] :set-nickname
         #_[clear-nickname [:info :nickname] :removed] ; -> [:ui :chat] :disable-nickname
         [(inspect ">>>>>>>>") [:**] :*]]})
