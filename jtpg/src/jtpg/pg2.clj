(ns jtpg.pg2
  (:import knossos.core.Model)
  (:require [cheshire.core :as json]
            [clojure.tools.logging  :refer [info]]
            [jepsen.checker :as checker]
            [jepsen.client :as client]
            [jepsen.control :as c]
            [jepsen.control.util :as cutil]
            [jepsen.db :as db]
            [jepsen.util :as jutil]
            [jepsen.util :refer [meh]]
            [knossos.core :as knossos]
            [org.httpkit.client :as http]))


(def repo-name "jtpg")
(def git-repo (format "https://github.com/AeroNotix/%s.git" repo-name))
(def git-dir (str "/tmp/" repo-name))

(defn erl-release-cmd [cmd]
  (meh (c/exec (c/lit (format "/tmp/jtpg/erl/_rel/jtpg/bin/jtpg %s"
                        cmd)))))

(defn kill-release []
  (meh (c/exec :killall :-9 "beam.smp" "epmd")))

(def db
  (reify db/DB
    (setup! [_ test node]
      (meh (c/exec :rm :-r "/tmp/jtpg/"))
      (c/cd "/tmp"
        (c/exec :git :clone git-repo))
      (c/cd (str "/tmp/" repo-name "/erl")
        (meh (erl-release-cmd "stop"))
        (kill-release)
        (c/exec :make :deps :compile node)
        (erl-release-cmd "start")
        (Thread/sleep 60000)))

    (teardown! [_ _ _]
      (kill-release))))

(defn create-new-pid [client n]
  (let [uri (str (.endpoint client) n)
        resp @(http/get uri)]
    (= (:status resp) 200)))

(defn filter-history-with [history f]
  (->> history
    (filter f)
    (filter #(= :add (:f %)))
    (map :value)
    (frequencies)))

(defn cmp-dupbag-ops* [[a av] [b bv]]
  (if (not= a b)
    (throw (IllegalArgumentException.
             "operation list is missing matching pair."))
    {({1 :gt 0 :eq -1 :lt} (compare av bv)) [a]}))

(defn cmp-dupbag-ops [first second]
  (merge-with concat (map cmp-dupbag-ops* first second)))

(def duplicate-bag-check
  (reify checker/Checker
    (check [this test model history]
      (let [invokes    (filter-history-with history knossos/invoke?)
            adds       (filter-history-with history knossos/ok?)
            final-read (->> history
                         (filter knossos/ok?)
                         (filter #(= :read (:f %)))
                         (map :value)
                         first)
            {:keys [gt eq lt]} (cmp-dupbag-ops adds final-read)]
        {:ok         (jutil/integer-interval-set-str (set eq))
         :lost       (jutil/integer-interval-set-str (set lt))
         :unexpected (jutil/integer-interval-set-str (set gt))}))))

(defrecord DuplicateBag [s]
  Model
  (step [this op]
    (case (:f op)
      :add (DuplicateBag.
             (update-in s [(:value op)]
               (fn [x] (nil? x) 0 (inc x))))
      :read (if (= s (:value op))
              this
              (knossos/inconsistent (str "State mismatch: " s " vs. "
                                      (:value op)))))))

(defn duplicate-bag []
  (DuplicateBag. {}))

(defrecord Pg2NodeListClient [endpoint]
  client/Client
  (setup! [_ _ node]
    (let [endpoint (str "http://" (name node) ":8080/new_pid/")]
      (Pg2NodeListClient. endpoint)))
  
  (invoke! [this test op]
    (case (:f op)
      :add (let [{:keys [value op] :as cmd} op]
             (let [resp (create-new-pid this value)]
               (if resp
                 (assoc cmd :type :ok)
                 (assoc cmd :type :fail))))
      :read (throw (IllegalArgumentException.))))

  (teardown! [_ _]))

(defn create-node-list-client []
  (Pg2NodeListClient. nil))
