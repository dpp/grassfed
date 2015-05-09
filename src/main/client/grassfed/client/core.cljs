(ns grassfed.core
  (:require [cljsjs.fixed-data-table]
            [re-com.core :refer [h-box v-box box gap line scroller border horizontal-tabs horizontal-bar-tabs
                                 vertical-bar-tabs horizontal-pill-tabs vertical-pill-tabs label button
                                 single-dropdown p]]
            [re-com.tabs :refer [tabs-args-desc]]
            [re-com.util :refer [item-for-id]]
            [cljs.core.async :as async]
            [cognitect.transit :as t]
            [reagent.core :as reagent :refer [atom adapt-react-class render-component]])
  (:require-macros [cljs.core.async.macros :as async])
  )

(enable-console-print!)



(def t-reader (t/reader :json))

(def t-writer (t/writer :json))

(defn t-read
  [msg]
  (t/read t-reader msg))

(defn t-write
  [msg]
  (t/write t-writer msg))

(def error-marker :grassfed:error)

(defn streaming-promise-to-channel
  "Takes a Streaming Promise and returns a channel"
  [promise]
  (let [the-chan (async/chan)]
    (-> promise
        (.then (fn [msg] (->> msg t-read (async/put! the-chan))))
        (.done (fn [] (async/close! the-chan)))
        (.fail (fn [err-str] (async/put! the-chan {error-marker err-str}))))
    the-chan)
  )

(defn send-to-server
  "Send a message to the server and expect a core.async channel back"
  [msg]
  (->> msg t-write (.send js/CljBridge) streaming-promise-to-channel)
  )

(defonce app-state (atom {:text "H33ello, what is your name? "}))

(defonce the-name (atom "dog"))

(defonce chats (atom []))

(def table (adapt-react-class (.-Table js/FixedDataTable)))
(def column (adapt-react-class (.-Column js/FixedDataTable)))


(defonce tabs-definition
  (atom
   [{:id ::tab1  :label "Tab1"  :data (atom [["Tab1" 33 44]])}
    {:id ::tab2  :label "Tab2"  :data (atom [["Tab2" 9893 344]]) }
    {:id ::tab3  :label "Tab3"  :data (atom [["Tab3" 1 0]])}]))

(def selected-tab-id (atom (-> @tabs-definition first :id)))

(defn show-table
  [info]
  (letfn [(get-row [x] (clj->js (get @info x)))]
    [table {:height 300 :width 600 :rowGetter get-row :rowsCount (count @info)
            :rowHeight 30 :headerHeight 50}
     [column {:width 300 :dataKey 0 :label "dog" }]
     [column {:width 300 :dataKey 1 :label "dog2" }]
     [column {:width 300 :dataKey 2 :label "dog3" }]])
)

(defn change-tab [x]
  (reset! selected-tab-id x))

(defn by-id [id] (. js/document (getElementById id)))

(defn send
  "send data to the server"
  [data]
  (js/sendToServer (t/write t-writer data)))

(defn send-chat
  []
  (let [box (by-id "in")]
    (send (.-value box))
    (set! (.-value box) "")
    ))

(set! (.-onclick (by-id "send")) send-chat )

(defn receive [x]
  (let
    [msg (t/read t-reader x)]
    (cond
      (seq? msg)
      (reset! chats (vec msg))

      (string? msg)
      (swap! chats conj msg)

      :else nil))
  )

(defn add-rows []
      (let [the-atom (:data  (item-for-id @selected-tab-id @tabs-definition))
            base (count @the-atom)]
           (doseq [x (range 0 40000)]
                  (swap! the-atom conj [(+ base x) "moose" "dog2"]))))

(defn remove-rows []
      (let [the-atom (:data  (item-for-id @selected-tab-id @tabs-definition))
            base (count @the-atom)]
           ;; (println "base / 2" (int (/ base 2)))
           (reset! the-atom (subvec @the-atom (int (/ base 2))))
           ))

(defn add-tab []
  (let [cnt (inc  (count @tabs-definition))]
    (swap! tabs-definition conj {:id (keyword (str ":grassfed.core:tab" cnt))
                                 :label (str "Tab" cnt)
                                 :data (atom [[(str "Tab" cnt) cnt cnt]])})))

(defn run-visi-line []
  (let [input (by-id "visiline")
        text (.-value input)
        res (send-to-server {:target :visi :text text})]
    ;; (set! (.-value input) "")
    (async/go-loop
      []
      (when-some
        [info (async/<! res)]
        (println "Got message " (pr-str info))
        (recur)
        )
      (println "Done!!")
      )
    ))


(defn page []
  [:div
   [:button {:onClick (fn [] (let [res (send-to-server {:target :ping})]
                               (async/go-loop
                                 []
                                 (when-some
                                   [info (async/<! res)]
                                   (println "Got message " (pr-str info))
                                   (recur)
                                    )
                                 (println "Done!!")
                                 )
                               ))} "Dogs!!"]
   [:hr]
   [:input {:id "visiline"}]
   [:button
    {:onClick run-visi-line}
    "Eval"]
   [:hr]
   [:div (@app-state :text) " " @the-name]
   [horizontal-tabs
    :model     selected-tab-id
    :tabs      @tabs-definition
    :on-change change-tab
    ]
   (doall
    (map (fn [row]
           ^{:key (:id row)}
           [:div {:style
                  {:display
                   (if (= @selected-tab-id (:id row))
                     "block" "none")}}
            (show-table (:data row))]) @tabs-definition))
   [:button {:onClick add-rows} "Add rows"]
   [:button {:onClick remove-rows} "Remove rows"]
   [:button {:onClick add-tab} "Add Tab"]
   [:hr]
   [:ul
    (doall (map (fn [x] ^{:key (str (rand))} [:li x]) (take-last 10 @chats)))]

   ])

(defn main []
  (render-component [page] (.getElementById js/document "app")))

(main)
