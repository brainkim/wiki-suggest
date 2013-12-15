(defproject wiki-suggest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/core.match "0.2.0"]
                 [org.clojure/data.json "0.2.3"] 
                 [seesaw "1.4.4"]
                 [http-kit "2.1.13"]]
  :main wiki-suggest.core)
