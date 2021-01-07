(defproject authtest "0.1.0-SNAPSHOT"
  :description "an example authentication service"
  :license {:name "ISC"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [aleph "0.4.6"]
                 [ring "1.8.2"]
                 [ring/ring-json "0.5.0"]
                 [bidi "2.1.6"]
                 [caesium "0.14.0"]
                 [buddy "2.0.0"]
                 [org.clojure/java.jdbc "0.7.1"]
                 [org.xerial/sqlite-jdbc "3.34.0"]
                 [com.layerware/hugsql "0.5.1"]]
  :main ^:skip-aot authtest.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
