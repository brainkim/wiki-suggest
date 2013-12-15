(ns wiki-suggest.search
  (:require [clojure.string :as str]
            [org.httpkit.client :as client]
            [clojure.core.async :as async :refer [chan close! put! map<]]
            [clojure.core.match :refer [match]]
            [clojure.data.json :refer [read-str]]))

;; URI settings
(def ^:dynamic *base-url* "http://en.wikipedia.org/w/api.php")
(defn- suggest-params 
  [term]
  {:action "opensearch"
   :format "json"
   :limit 15
   :search term})
(defn- search-params
  [term]
  {:action "mobileview"
   :format "json"
   :sections "0"
   :noimages true
   :prop "text"
   :page term})

;; http://en.wikipedia.org/wiki/Wikipedia:Naming_conventions_(technical_restrictions)#Spaces_and_underscores
(defn sanitize
  [dirty]
  (async/map< (fn [s] (-> s str/trim (str/replace #"\s+" "_"))) dirty))

;; channel functions
;; TODO: Something isn't right here
(defn- query-api
  [params]
  (let [out (chan)
        close #(close! out)]
    (client/get *base-url* {:query-params params}
                (fn [{:keys [status body error]}]
                  (println status)
                  (if error
                    (put! out [:error status error] close)
                    (put! out (read-str body :key-fn keyword) close))))
    out))

(defn suggest
  [term]
  (->> term
       suggest-params
       query-api
       (map< (fn [vs]
               (match vs
                 [:error status error] [(str "Failed to connect." status)
                                        error]
                 :else                 (nth vs 1))))))

(defn search
  [term]
  (->> term
       search-params
       query-api
       (map< (fn [res]
               (match res
                  [:error status error] (str "Failed to connect." status error)
                  :else                 (->> res
                                             :mobileview
                                             :sections
                                             (map :text)
                                             (reduce str)))))))
