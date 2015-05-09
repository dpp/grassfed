(ns grassfed.server.bridge
  (:require [grassfed.server.util :as gu]
            [visi.core.parser :as vp]
            [clojure.core.async :as async :refer [put! close!]])
  (:import (net.liftweb.http RoundTripHandlerFunc)
           (net.liftweb.util Helpers Helpers$)))

(defonce message-handlers (atom {}))

(def ^:dynamic **server-target** nil)

(defn process
  "Process a message and a `RoundTripHandlerFunc`"
  [server-target msg ^RoundTripHandlerFunc hanlder]
  (binding [**server-target** server-target]
    (if-some
      [func (some->> msg :target (get @message-handlers))]
      (let [the-chan (gu/rt-handler-to-channel hanlder)]
        (try
          (func msg the-chan)
          (catch Exception e
            (do
              (.failure hanlder (pr-str e))
              (close! the-chan)
              ))
          )
        )
      (.failure hanlder (str "Couldn't find a handler for: " (pr-str msg)))
      )))

(defn register-handler
  "Registers a handler"
  [the-key the-func]
  (swap! message-handlers assoc the-key the-func)
  )

(defn pong
  "A pong that responds to a ping"
  [msg the-chan]
  (let [target **server-target**]
    (future
      (println "Server target " **server-target**)
      (put! the-chan ["pong 1" msg])
      (Thread/sleep 300)
      (put! the-chan ["pong 2" msg])
      (Thread/sleep 300)
      (put! the-chan ["pong 3" msg])
      (Thread/sleep 300)
      (put! the-chan ["pong 4" msg])
      (Thread/sleep 300)
      (gu/send! target {:target :ping :date (java.util.Date.) :text (str "Wombat " (.nextFuncName (Helpers$/MODULE$)))})
      (close! the-chan))))

(register-handler :ping pong)


(defn exec-visi
  [msg the-chan]
  (future
    (try
      (when-some
        [text (:text msg)]
        (->> (vp/parse-and-eval-for-tests text) (put! the-chan))
        )
      (catch Exception e (put! the-chan {:grassfed:error (.getMessage e)}))
      (finally (close! the-chan)))))

(register-handler :visi exec-visi)