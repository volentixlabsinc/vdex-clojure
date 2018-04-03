(ns libvtx.tokens-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.io   :as io]
   [clojure.java.jdbc :as jdbc]
   [duct.core         :as duct]
   [duct.logger       :as logger]
   [integrant.core    :as ig]
   [fipp.edn :refer [pprint]]
   [libvtx.token :as token]))

(duct/load-hierarchy)

(defrecord TestLogger []
  logger/Logger
  (-log [_ level ns-str file line id event data]))

;; fake logger initialization
;; we don't need whole logger subsystem
(defmethod ig/init-key :duct/logger [_ config] (->TestLogger))

(defn system []
  (-> "libvtx/config.edn"
      io/resource
      duct/read-config
      duct/prep
      ig/init))

(defn ->db-spec [system]
  (-> system :duct.database.sql/hikaricp :spec))

(deftest tokens-test
  (let [sys (system)
        db-spec (->db-spec sys)
        address (token/create db-spec "ETH")]
    (is (= address
           (:address (token/get db-spec address))))
    (ig/halt! sys)))
