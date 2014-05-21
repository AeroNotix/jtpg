(ns jtpg.pg2-test
  (:use [clojure.pprint]
        [jepsen.core])
  (:require [clojure.test     :refer :all]
            [jepsen.checker   :as checker]
            [jepsen.checker.timeline :as timeline]
            [jepsen.client    :as client]
            [jepsen.db        :as jdb]
            [jepsen.generator :as gen]
            [jepsen.model     :as model]
            [jepsen.nemesis   :as nemesis]
            [jepsen.os        :as os]
            [jepsen.os.debian :as debian]
            [jepsen.report    :as report]
            [jepsen.store     :as store]
            [jtpg.pg2         :refer :all]))


(defn randomly-duplicate
  ([coll]
     (randomly-duplicate coll 5))
  ([coll chance]
     (randomly-duplicate coll chance []))
  ([[hd & tl] chance curr]
     (if (nil? hd)
       (reverse curr)
       (if (<= (rand-int 100) chance)
         (recur tl chance (cons hd (cons hd curr)))
         (recur tl chance (cons hd curr))))))

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
                  :model (duplicate-bag)
                  :nemesis (nemesis/partition-random-halves)
                  :checker (checker/compose {:html    timeline/html
                                             :dupbag  duplicate-bag-check})
                  :generator (gen/phases
                              (->> (range 600)
                                   (map (fn [x] {:type  :invoke
                                                 :f     :add
                                                 :value x}))
                                   randomly-duplicate
                                   gen/seq
                                   (gen/delay 1)
                                   (gen/nemesis
                                     (gen/seq
                                       (cycle [(gen/sleep 15)
                                               {:type :info :f :start}
                                               (gen/sleep 30)
                                               {:type :info :f :stop}])))
                                   (gen/time-limit 200))
                              (gen/nemesis
                                (gen/once {:type :info :f :stop}))
                              (gen/sleep 60) ; to allow nodes to recluster.
                              (gen/clients
                                (gen/once {:type :invoke :f :read})))})]
      (pprint (:results test)))))
