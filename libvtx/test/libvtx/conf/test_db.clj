(ns libvtx.conf.test-db
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.java.io :as io]
    [clj-time.jdbc]
    [duct.core :as duct]
    [integrant.core :as ig]))


(def test-db-connection
  ^{:doc  "It keeps a connection to test DB"}
  (atom nil))


(def ^:dynamic
  ^{:doc "It keeps a configuration with test DB connection"}
  *conf* nil)


(def system
  ^{:doc "Keeps initialized system."}
  (atom nil))


(defn init-system!
  []
  (let [config (-> "libvtx/config.edn" io/resource duct/read-config)
        test-db-url (get-in config [:libvtx.handler/api :test-database-url])
        test-config (-> config
                        (assoc-in [:duct.module/sql :database-url] test-db-url)
                        (assoc-in [:duct.database.sql/hikaricp :jdbc-url] test-db-url))
        duct-migrator [:duct.migrator/ragtime]]

    ; each time when migrator is initialized it creates HikariCP connections pool and also it runs migrations if needed
    ; after finished test we stop migrator so pool is cleaned up (closed)
    ; this way we shouldn't exceed limited number of available db connections after run test many times
    ; we avoid org.postgresql.util.PSQLException: FATAL: sorry, too many clients already
    (duct/load-hierarchy)
    (-> test-config
        (duct/prep duct-migrator)
        (ig/init duct-migrator))))


(defn create-connection!
  "Create a connection to the test DB for testing."
  [test-fn]
  (when (nil? @test-db-connection)
    (let [config (-> "libvtx/config.edn" io/resource duct/read-config)
          db-link (get-in config [:libvtx.handler/api :test-database-url])]
      (reset! test-db-connection (jdbc/get-connection {:connection-uri db-link}))
      (.setAutoCommit @test-db-connection false)))
  (try
    (reset! system (init-system!))
    (test-fn)
    (catch Exception e
      (do
        (.printStackTrace e)
        (throw e)))
    (finally
      (when @system

        (-> @system :duct.database.sql/hikaricp :spec :datasource .close)
        (reset! system (ig/halt! @system [:duct.migrator/ragtime]))
        (reset! system nil)


        (.close @test-db-connection)
        (reset! test-db-connection nil)))))


(defn assoc-test-db
  [db-spec]
  (-> "libvtx/config.edn"
      io/resource
      duct/read-config
      (get :libvtx.handler/api)
      (assoc :db {:spec db-spec})))


(defn with-test-db
  "Wraps a function call with binding to test DB."
  [test-fn]
  (jdbc/with-db-transaction [db-spec {:connection (deref test-db-connection)
                                      :datasource (-> @system :duct.database.sql/hikaricp :spec :datasource)}
                             {:isolation :serializable}]
    (jdbc/db-set-rollback-only! db-spec)
    (binding [*conf* (assoc-test-db db-spec)]
      (test-fn))))
