(defproject grassfed "0.1.0-SNAPSHOT"
  :description "Some nifty Scala/Clojure/Lift stuff"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/main/clj" "src/main/scala" "src/main/client"]

  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [reagent "0.5.0"]
                 [cljsjs/fixed-data-table "0.1.2-2"]
                 [re-com "0.5.3"]
                 [org.scala-lang/scala-library "2.11.6"]
                 [net.liftweb/lift-webkit_2.11 "3.0-SNAPSHOT"]
                 [net.liftweb/lift-mapper_2.11 "3.0-SNAPSHOT"]
                 [org.eclipse.jetty/jetty-webapp "8.1.7.v20120910"]
                 [org.eclipse.jetty/jetty-plus "8.1.7.v20120910"]
                 [org.eclipse.jetty.orbit/javax.servlet  "3.0.0.v201112011016"]
                 [ch.qos.logback/logback-classic "1.0.6"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.h2database/h2 "1.3.167"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.cognitect/transit-clj "0.8.271"]
                 [org.clojure/java.classpath "0.2.2"]
                 ]

  :repositories [["snapshots"     "https://oss.sonatype.org/content/repositories/snapshots"],
                 ["staging"       "https://oss.sonatype.org/content/repositories/staging"],
                 ["releases"      "https://oss.sonatype.org/content/repositories/releases"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-idea "1.0.1"]
            [lein-zinc "0.1.4"]
            [lein-figwheel "0.3.1"]]

  :zinc-options {:inputs {:sources ["src/main/scala"]}}

  :min-lein-version "2.5.0"

  :figwheel {
   :nrepl-port 7888
   }


  :cljsbuild {:builds {:app {:source-paths ["src/main/client"]
                             :figwheel true
                             :compiler {:output-to "src/webapp/gen_js/js/app.js"
                                        :output-dir "src/webapp/gen_js/js/out"
                                        :source-map    "src/webapp/gen_js/js/out.js.map"
                                        :optimizations :none}}}}
  )
