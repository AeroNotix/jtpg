(ns jtpg.pg2
  (:require [cheshire.core :as json]
            [clojure.tools.logging  :refer [info]]
            [jepsen.client :as client]
            [jepsen.control :as c]
            [jepsen.control.util :as cutil]
            [jepsen.db :as db]
            [org.httpkit.client :as http]))


(def repo-name "jtpg")
(def git-repo (format "https://github.com/AeroNotix/%s.git" repo-name))
(def git-dir (str "/tmp/" repo-name))

(def db
  (reify db/DB
    (setup! [_ test node]
      (when (not (cutil/file? git-dir))
        (info node "Downloading git repo.")
        (c/su
          (c/cd "/tmp"
            (c/exec :git :clone git-repo))))
      (c/cd (str "/tmp/" repo-name "/erl")
        (info "Location of release: " (str "/tmp/" repo-name "/erl"))
        (c/lit "./_rel/jtpg/bin/jtpg stop")
        (info node "building jtpg release.")
        (c/exec :make)
        (c/exec :make :release)
        (c/lit "/tmp/jtpg/erl/_rel/jtpg/bin/jtpg start")
        (Thread/sleep 5000)))

    ;; TODO: Stop the Erlang node.
    (teardown! [_ _ _])))

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
