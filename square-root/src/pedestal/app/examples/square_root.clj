;; Copyright (c) 2012 Relevance, Inc. All rights reserved.

(ns pedestal.app.examples.square-root
  (:use pedestal.app)
  (:require [pedestal.app.protocols :as p]))

;; Create a model function which will be used for each of the three
;; models. All inputs are numbers. The model doesn't do anything but
;; return the number it was given.

(defn number-model [old event]
  (if (= (:event event) :init) (:value event) (:n event)))

;; Create the functions which will be used as "views". Each of these is
;; a pure function.

(defn sum [state inputs]
  (let [ns (keep :new (vals inputs))]
    (if (some #(= % :NaN) ns)
      :NaN
      (apply + ns))))

(defn divider [dividend divisor]
  (fn [state inputs]
    (let [dividend (:new (get inputs dividend)) 
          divisor (:new (get inputs divisor))]
      (if (== divisor 0)
        :NaN
        (double (/ dividend divisor))))))

(defn half [state input-name old new]
  (if (= new :NaN)
    :NaN
    (double (/ new 2))))

(defn good-enough? [state inputs]
  (let [{:keys [accuracy half]} inputs
        [old-acc new-acc] ((juxt :old :new) accuracy)
        [old-guess new-guess] ((juxt :old :new) half)]
    (if (some #(= % :NaN) [new-acc new-guess old-guess])
      {:good-enough? false :new-guess new-guess}
      (let [good-enough? (and (> new-acc (Math/abs (- new-guess old-guess)))
                              (= old-acc new-acc))
            new-guess (cond (not= old-acc new-acc) (- new-guess new-acc)
                            :else new-guess)]
        {:good-enough? good-enough? :new-guess new-guess}))))

;; Create an "events" function which will be used to generate new
;; events which will cause the calculation to continue.

(defn continue-calc [view-name o n]
  (when (not (or (:good-enough? n)
                 (= (:new-guess n) :NaN)))
    [{:to :guess :n (:new-guess n)}]))

;; Configure the applcation.

(def square-root-app
  {:models  {:guess    {:init 0 :fn number-model}
             :x        {:init 0 :fn number-model}
             :accuracy {:init 0 :fn number-model}}
   :views   {:divide       {:fn (divider :x :guess)
                            :input #{:x :guess}}
             :sum          {:fn sum :input #{:guess :divide}}
             :half         {:fn half :input #{:sum}}
             :good-enough? {:fn good-enough? :input #{:half :accuracy}}}
   :events  {:good-enough? continue-calc}
   :screens {:answer {:fn default-screen-fn :input #{:x :half}}}})

;; Create a Renderer which will print to the console.

(defrecord ConsoleRenderer []
  p/Renderer
  (render [this deltas]
    (doseq [d deltas]
      (println d))))

;; Create the application.

(defn make-app []
  (let [app (build square-root-app)]
    (set-renderer app (->ConsoleRenderer))
    (begin app)
    app))

(comment

  ;; As new inputs are sent to the app, the UI deltas are printed.
  
  (def app (make-app))
  ;;=> [:node-enter [] :map]
  ;;=> [:node-enter [:half] :map]
  ;;=> [:value-enter [:half] :NaN]
  ;;=> [:node-enter [:x] :map]
  ;;=> [:value-enter [:x] 0]

  (run! app [{:to :accuracy :n 0.01}])
  ;;=> nil

  (run! app [{:to :x :n 42}])
  ;;=> [:value-update [:x] 0 42]

  (run! app [{:to :guess :n 10}])
  ;;=> [:value-update [:half] :NaN 7.1]
  ;;=> [:value-update [:half] 7.1 6.507746478873239]
  ;;=> [:value-update [:half] 6.507746478873239 6.480796732565069]
  ;;=> [:value-update [:half] 6.480796732565069 6.4807406986501]

  (run! app [{:to :accuracy :n 0.000001}])
  ;;=> [:value-update [:half] 6.4807406986501 6.480740698407937]

  (run! app [{:to :x :n 50}])
  ;;=>[:value-update [:x] 42 50]
  ;;=>[:value-update [:half] 6.480740698407937 7.097954193471344]
  ;;=>[:value-update [:half] 7.097954193471344 7.071118733405411]
  ;;=>[:value-update [:half] 7.071118733405411 7.0710678120488275]
  ;;=>[:value-update [:half] 7.0710678120488275 7.0710678118654755]

  )
