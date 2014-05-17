(defproject AeroNotix/jtpg "0.0.1"
  :description "Jepsen testing various Erlang process-group features
  and implementations "
  :url "http://github.com/AeroNotix/jtpg"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
