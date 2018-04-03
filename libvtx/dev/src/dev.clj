(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [fipp.edn :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [duct.core :as duct]
            [duct.core.repl :as duct-repl]
            [eftest.runner :as eftest]
            [integrant.core :as ig]
            [integrant.repl :refer [clear halt go init prep reset]]
            [integrant.repl.state :refer [config system]]
            [libvtx.crypto :as crypto]
            [libvtx.token :as token]
            [libvtx.db :as db]))

(duct/load-hierarchy)

(defn read-config []
  (duct/read-config (io/resource "dev.edn")))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")

(when (io/resource "local.clj")
  (load "local"))

(integrant.repl/set-prep! (comp duct/prep read-config))

(defn db-spec []
  (-> system :duct.database.sql/hikaricp :spec))

;; === common stuff ================
(defn all-db-tables []
  (db/get-tables (db-spec)))

;; === tokens ======================
(comment
  (all-db-tables)

  (token/get-all (db-spec))

  (let [db-spec (db-spec)
        address (token/create db-spec "ETH")]
    (token/get db-spec address))

)
