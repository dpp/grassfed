(defproject grassfed "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [reagent "0.5.0"]
                 [cljsjs/fixed-data-table "0.1.2-2"]
                 [re-com "0.5.3"]
                 ]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.1"]]

  :figwheel
  {
   :nrepl-port 7888
   }

  :cljsbuild {:builds {:app {:source-paths ["src"]
                             :figwheel true
                             :compiler {:output-to "resources/js/app.js"
                                        :output-dir "resources/js/out"
                                        :source-map    "resources/js/out.js.map"
                                        :optimizations :none}}}}
  )
