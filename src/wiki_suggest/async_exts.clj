(ns wiki-suggest.async-exts
  (:refer-clojure :exclude [distinct concat])
  (:require [clojure.core.async :refer [<! >! go-loop chan close!]]))

(defn distinct
  "Takes a channel and filters out repeating values"
  [ch]
  (let [out (chan)]
    (go-loop [prev nil]
      (let [curr (<! ch)]
        (when (not= curr prev)
          (>! out curr))
        (recur curr)))
    out))

(defn concat
  "Takes a collection of source channels and returns a channel which will take
  all values from each channel until all channels are exhausted. The returned
  channel is unbuffered by default, or a buf-or-n can be supplied."
  ([chs]
    (concat chs nil))
  ([chs buf-or-n]
    (let [out (chan buf-or-n)]
      (go-loop [chs' chs]
        (if (empty? chs')
          (close! out)
          (let [v (<! (first chs'))]
            (if (nil? v)
              (recur (rest chs'))
              (do 
                (>! out v)
                (recur chs')))))))))
