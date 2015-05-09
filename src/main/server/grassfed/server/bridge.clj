(ns grassfed.server.bridge
  (:require [grassfed.server.util :as gu]
            [visi.core.parser :as vp]
            [clojure.core.async :as async :refer [put! close!]])
  (:import (net.liftweb.http RoundTripHandlerFunc)))

(defonce message-handlers (atom {}))

(defn process
  "Process a message and a `RoundTripHandlerFunc`"
  [msg ^RoundTripHandlerFunc hanlder]
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
    ) )

(defn register-handler
  "Registers a handler"
  [the-key the-func]
  (swap! message-handlers assoc the-key the-func)
  )

(defn pong
  "A pong that responds to a ping"
  [msg the-chan]
  (future
    (put! the-chan ["pong 1" msg])
    (Thread/sleep 300)
    (put! the-chan ["pong 2" msg])
    (Thread/sleep 300)
    (put! the-chan ["pong 3" msg])
    (Thread/sleep 300)
    (put! the-chan ["pong 4" msg])
    (Thread/sleep 300)
    (close! the-chan)))

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