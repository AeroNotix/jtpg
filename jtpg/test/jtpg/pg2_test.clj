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


(def noop-test
  "Boring test stub"
  {:nodes     [:n1 :n2 :n3 :n4 :n5]
   :os        os/noop
   :db        jdb/noop
   :client    client/noop
   :nemesis   client/noop
   :generator gen/void
   :model     model/noop
   :checker   checker/linearizable})

(deftest group-test
  (binding [jepsen.control/*username* "root"
            jepsen.control/*password* "root"
            jepsen.control/*strict-host-key-checking* :no]
    (let [sample-size 25000
          test (run!
                 (assoc noop-test
                   :name "pg2"
                   :os debian/os
                   :db db
                   :client (create-node-list-client)
                   :model (model/set)
                   :nemesis (nemesis/partition-random-halves)
                   :generator (gen/phases
                                (->> (range sample-size)
                                  (map (fn [x] {:type  :invoke
                                                :f     :add
                                                :value x}))
                                  (gen/seq)
                                  (gen/nemesis
                                    (gen/seq
                                      (cycle [(gen/sleep 5)
                                              {:type :info :f :start}
                                              (gen/sleep 5)
                                              {:type :info :f :stop}])))))))]

      (println test))))
