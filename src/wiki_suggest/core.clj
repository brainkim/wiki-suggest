(ns wiki-suggest.core
  (:require [clojure.string :refer [blank?]]
            [seesaw.core :as ss]
            [wiki-suggest.gui :as gui]
            [wiki-suggest.search :as search]
            [wiki-suggest.async-exts :as async-exts]
            [clojure.core.async :refer [<! go-loop alts! close!]] ))

(defn -main
  []
  ;; TODO: DRY?
  (let [terms (-> gui/search-terms
                  search/sanitize
                  async-exts/distinct)]
    (go-loop [term nil]
      (if term
        (let [suggestions (search/suggest term)
              [v winner] (alts! [terms suggestions])]
          (condp = winner
            terms
              (do (close! suggestions)
                  (recur v))
            suggestions
              (do
                (gui/set-suggestion! v)
                (recur nil))))
        (recur (<! terms)))))

  (let [queries (-> gui/search-queries
                     search/sanitize
                     async-exts/distinct)]
    (go-loop [query nil]
      (if query
        (let [results (search/search query)
              [v winner] (alts! [queries results])]
          (condp = winner
            queries
              (do (close! results)
                  (recur v))
            results
              (if-not (blank? v)
                (do
                  (gui/set-view! v)
                  (recur v))
                (recur query))))
        (recur (<! queries)))))
  (gui/show!))
