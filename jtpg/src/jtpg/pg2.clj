(ns jtpg.pg2
  (:require [jepsen.db :as db]
            [jepsen.control :as c]
            [org.httpkit.client :as http]))


(def repo-name "jtpg")
(def git-repo (format "https://github.com/AeroNotix/%s.git" repo-name))

(def db
  (reify db/DB
    (setup! [_ test node]
      (c/su
        (c/cd "/tmp"
          (c/exec :git :clone git-repo))
        (c/cd (str "/tmp/" git-repo)
          (c/exec :make))))
    (teardown! [_ test node]
      (c/su
        (c/exec :rm :-rf (str "/tmp/" repo-name))))))
(defn create-new-pid [client n]
  (let [uri (str (.endpoint client) n)
        resp @(http/get uri)]
    (= (:status resp) 200)))

(defrecord Pg2NodeListClient [endpoint]
  client/Client
  (setup! [_ _ node]
    (let [endpoint (str "http://" (name node) ":8080/new_pid/")]
      (Pg2NodeListClient. endpoint)))

  (invoke! [_ test op]
    (throw (UnsupportedOperationException.))))

(defn create-node-list-client []
  (Pg2NodeListClient. nil))
