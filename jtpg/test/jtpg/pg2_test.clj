(ns jtpg.pg2-test
  (:use     [jepsen.core])
  (:require [clojure.test     :refer :all]
            [jepsen.checker   :as checker]
            [jepsen.db        :as jdb]
            [jepsen.generator :as gen]
            [jepsen.model     :as model]
            [jepsen.nemesis   :as nemesis]
            [jepsen.os        :as os]
            [jepsen.client    :as client]
            [jepsen.os.debian :as debian]
            [jepsen.report    :as report]
            [jepsen.store     :as store]
            [jtpg.pg2         :refer :all]))


(deftest group-test
  (binding [jepsen.control/*username* "root"
            jepsen.control/*password* "root"
            jepsen.control/*strict-host-key-checking* :no]
    (let [sample-size 25000
          test (run!
                 {:nodes [:n1 :n2 :n3 :n4 :n5]
                  :name "pg2"
                  :os debian/os
                  :db db
                  :client (create-node-list-client)
                  :model duplicate-bag
                  :nemesis (nemesis/partition-random-halves)
                  :checker duplicate-bag
                  :generator (gen/phases
                              (->> (range)
                                   (map (fn [x] {:type  :invoke
                                                 :f     :add
                                                 :value x}))
                                   gen/seq
                                   (gen/stagger 1/10)
                                   (gen/delay 1)
                                   (gen/nemesis
                                     (gen/seq
                                       (cycle [(gen/sleep 60)
                                               {:type :info :f :start}
                                               (gen/sleep 300)
                                               {:type :info :f :stop}])))
                                   (gen/time-limit 600))
                              (gen/nemesis
                                (gen/once {:type :info :f :stop}))
                              (gen/clients
                                (gen/once {:type :invoke :f :read})))})]
      (println test))))
