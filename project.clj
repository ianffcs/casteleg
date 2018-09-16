(defproject casteleg "0.1.0-SNAPSHOT"
  :dependencies [[etaoin/etaoin "0.2.8"]
                 [org.clojure/clojure "1.10.0-alpha8"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/data.json "0.2.6"]]
  :profiles {:test {:dependencies [[midje/midje "1.9.2"]]}})
