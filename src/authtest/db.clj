(ns authtest.db
  (:require [clojure.java.jdbc :as j]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.set :as set]
            [clojure.pprint :as pp]
            [hugsql.core :as hugsql]))

(def db {:classname "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname (or (System/getenv "AUTHTEST_DATABASE")
                      (str (System/getProperty "user.home") "/.authtest.db"))})

(def tables #{"users"})

(def tables-loaded (into #{} (map #(let [p (str "authtest/db/sql/" % ".sql")]
                                     (hugsql/def-db-fns p) %)
                                  tables)))

(defn result->id
  "Convert SQLite :last_insert_rowid() map to :id map"
  [result]
  {:id (-> result ((keyword "last_insert_rowid()")))})

(defn create-all-tables
  "Create database tables. Expects each table listed in the `tables` set
  to have a corresponding create-`table`-table function defined."
  []
  (map #((-> (str "create-" % "-table") symbol resolve) db) tables))

(defn delete-and-recreate-database
  []
  (let [database-file (io/file (:subname db))]
    (when (.exists database-file)
      (.delete database-file))
    (create-all-tables)))
