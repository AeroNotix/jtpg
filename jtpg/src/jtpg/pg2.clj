(ns jtpg.pg2
  (:require [cheshire.core :as json]
            [clojure.tools.logging  :refer [info]]
            [jepsen.client :as client]
            [jepsen.control :as c]
            [jepsen.control.util :as cutil]
            [jepsen.db :as db]
            [jepsen.util :refer [meh]]
            [org.httpkit.client :as http]))


(def repo-name "jtpg")
(def git-repo (format "https://github.com/AeroNotix/%s.git" repo-name))
(def git-dir (str "/tmp/" repo-name))

(defn erl-release-cmd [cmd node]
  (meh (c/exec (c/lit (format "/tmp/jtpg/erl/_%s/jtpg/bin/jtpg %s" (name node) cmd)))))

(defn kill-release []
  (meh (c/exec :killall :-9 "beam.smp" "epmd")))

(def db
  (reify db/DB
    (setup! [_ test node]
      (meh (c/exec :rm :-r "/tmp/jtpg/"))
      (info node "Downloading git repo.")
      (c/cd "/tmp"
        (c/exec :git :clone git-repo))
      (c/cd (str "/tmp/" repo-name "/erl")
        (meh (erl-release-cmd "stop" node))
        (kill-release)
        (info "Location of release: " (str "/tmp/" repo-name "/erl"))
        (info node "building jtpg release.")
        (c/exec :make :compile)
        (c/exec :make node)
        (erl-release-cmd "start" node)
        (Thread/sleep 5000)))

    ;; TODO: Stop the Erlang node.
    (teardown! [_ _ _]
      (kill-release))))

(defn create-new-pid [client n]
  (let [uri (str (.endpoint client) n)
        resp @(http/get uri)]
    (info "Response status: " (:status resp) "response: " resp)
    (= (:status resp) 200)))

(defrecord Pg2NodeListClient [endpoint]
  client/Client
  (setup! [_ _ node]
    (let [endpoint (str "http://" (name node) ":8080/new_pid/")]
      (Pg2NodeListClient. endpoint)))
  
  (invoke! [this test {:keys [value op] :as cmd}]
      (let [resp (create-new-pid this value)]
        (info "Response from creating new pid:" resp)
        (if resp
          (assoc cmd :type :ok)
          (assoc cmd :type :fail))))

  (teardown! [_ _]
    (throw (UnsupportedOperationException.))))

(defn create-node-list-client []
  (Pg2NodeListClient. nil))
