(ns jtpg.pg2
  (:require [jepsen.db :as db]
            [jepsen.control :as c]
            [org.httpkit.client :as http]))


(def repo-name "jtpg")
(def git-repo (format "git@github.com:AeroNotix/%s.git" repo-name))

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

(defrecord Pg2NodeListClient [client]
  client/Client
  (setup! [_ _ node]
    (let [client (str "http://" (name node) ":8080")]
      (Pg2NodeListClient. client)))

  (invoke! [_ test op]
    (throw (UnsupportedOperationException.))))
