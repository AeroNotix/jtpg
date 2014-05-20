(defproject AeroNotix/jtpg "0.0.1"
  :description "Jepsen testing various Erlang process-group features
  and implementations "
  :url "http://github.com/AeroNotix/jtpg"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [jepsen "0.0.3-SNAPSHOT"]
                 [cheshire "5.3.1"]
                 [knossos "0.2"]
                 [http-kit "2.1.18"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
