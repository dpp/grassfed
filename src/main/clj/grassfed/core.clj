(ns grassfed.core
  (:require [grassfed.util :refer [send! to-c]]
            [clojure.core.match :refer [match]]
            [clojure.core.async :as async :refer [chan go put! <!]]))

(defonce chat-server (chan))

(defn post-msg
  "Posts a message to the chat-server"
  [msg]
  (->> msg to-c (send! chat-server)))

(defonce chats (atom []))

(defonce listeners (atom []))

(defn run-loop
  []
  (go
    (do
      (match
          (<! chat-server)
          [:add lst]
          (do
            (send! lst (take-last 40 @chats))
            (swap! listeners conj lst))

          [:remove lst]
          (swap! listeners (fn [info] (remove #(identical? lst %) info)))

          (msg :guard string?)
          (do
            (doseq [f @listeners] (send! f msg))
            (swap! chats conj msg))

          :else nil)
        (run-loop))))

(run-loop)

