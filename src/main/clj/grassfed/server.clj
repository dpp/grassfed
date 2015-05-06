(ns grassfed.server
  (:require [clojure.java.classpath :as cp])
  (:import [org.eclipse.jetty.server.handler ContextHandler]
           [org.eclipse.jetty.server.nio SelectChannelConnector]
           [org.eclipse.jetty.server Handler Server Connector]
           [org.eclipse.jetty.webapp WebAppContext WebAppClassLoader]

           (java.net URLClassLoader URL)
           (java.io File))
  )

;; Run the Jetty server

(def server-atom (atom nil))

(defn stop
  []
  (println "Stopping")
  (some-> @server-atom .stop .join)
  (reset! server-atom nil)
  (println "Stopped"))

(defn -main [& args]
  (when @server-atom (stop))
  (let [server (Server.)
        scc (SelectChannelConnector.)]
    (.setPort scc 8080)
    (.setConnectors server (into-array Connector [scc]))
    (let [context (WebAppContext.)]
      (.setWar context "src/webapp")
      (.setServer context server)
      (let [context0 (ContextHandler.)]
        (.setHandler context0 context)
        (.setHandler server context0)
        (reset! server-atom server)
        (.start server)))
    ))

