(ns libvtx.db.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [hugsql.core :as hugsql]
   [libvtx.hugsql.kebab-adapter :as adapter]))


(hugsql/def-db-fns "libvtx/db/db.sql" {:quoting :ansi
                                    :adapter (adapter/kebab-adapter)})
(hugsql/def-sqlvec-fns "libvtx/db/db.sql" {:quoting :ansi
                                        :adapter (adapter/kebab-adapter)})


(defn get-tables [db-spec]
  (jdbc/query db-spec ["SELECT name FROM sqlite_master WHERE type='table'"]))


(defn get-mempool-transactions
  [db-spec mempool-interval]
  (jdbc/query db-spec
              [(str "select * from transactions where mempool = 1 "
                    (format "and created_at < datetime('now', '-%d minutes')" mempool-interval))]))
