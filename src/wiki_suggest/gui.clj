(ns wiki-suggest.gui
  (:require [seesaw.core :as ss])
  (:require [clojure.core.async :as async
             :refer [<! >! chan alt! alts!! go put!]]))

;; UI components
(def search-box
  (ss/text
    :maximum-size [640 :by 20]))
(def suggestion-list
  (ss/listbox))
(def search-column
  (ss/top-bottom-split search-box (ss/scrollable suggestion-list)
    :drag-enabled? false
    :divider-size 0))
(def view
  (ss/editor-pane
    :content-type "text/html"
    :editable? false))
(def columns
  (ss/left-right-split search-column (ss/scrollable view)
    :drag-enabled? false
    :divider-size 0
    :divider-location 1/3))
(def main-frame
  (ss/frame
    :content columns
    :title "Query Wikipedia"
    :minimum-size [900 :by 600]))

;; Async channels
(def search-terms
  (let [out (chan)
        listener (fn [_]
                   (put! out (ss/value search-box)))]
    (ss/listen search-box
               :document listener)
    out))
(def search-queries
  (let [out (chan)
        listener (fn [_]
                   (when-let [sel (ss/selection suggestion-list)]
                     (put! out sel)))]
    (ss/listen suggestion-list
               :selection listener)
    out))

;; View mutators
(defn set-suggestion!
  [suggestions]
  (ss/config! suggestion-list :model suggestions))
(defn set-view!
  [page]
  (ss/config! view :text page))
(defn show!
  []
  (ss/native!)
  (ss/show! main-frame))
