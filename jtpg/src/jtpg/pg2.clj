(ns jtpg.pg2
  (:require [jepsen.db :as db]
            [jepsen.control.util :as cutil]
            [cheshire.core :as json]
            [jepsen.control :as c]
            [jepsen.client :as client]
            [org.httpkit.client :as http]))


(def repo-name "jtpg")
(def git-repo (format "https://github.com/AeroNotix/%s.git" repo-name))
(def git-dir (str "/tmp/" repo-name))

(def db
  (reify db/DB
    ;;  TODO: Run the Erlang node.
    (setup! [_ test node]
      (when (not (cutil/file? git-dir))
        (c/su
          (c/cd "/tmp"
            (c/exec :git :clone git-repo))))
      (c/cd (str "/tmp/" repo-name "/erl")
        (c/exec :make)))

    ;; TODO: Stop the Erlang node.
    (teardown! [_ _ _])))

(defn create-new-pid [client n]
  (let [uri (str (.endpoint client) n)
        resp @(http/get uri)]
    (= (:status resp) 200)))

(defrecord Pg2NodeListClient [endpoint]
  client/Client
  (setup! [_ _ node]
    (let [endpoint (str "http://" (name node) ":8080/new_pid/")]
      (Pg2NodeListClient. endpoint)))
  
  (invoke! [this test {:keys [value op] :as cmd}]
    (let [resp (create-new-pid this value)]
      (if resp
        (assoc cmd :type :ok)
        (assoc cmd :type :fail))))

  (teardown! [_ _]
    (throw (UnsupportedOperationException.))))

(defn create-node-list-client []
  (Pg2NodeListClient. nil))
