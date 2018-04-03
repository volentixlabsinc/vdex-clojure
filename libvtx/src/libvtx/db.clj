(ns libvtx.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [hugsql.core :as hugsql]
   [libvtx.hugsql.kebab-adapter :as adapter]))


(hugsql/def-db-fns "libvtx/db.sql" {:quoting :ansi
                                    :adapter (adapter/kebab-adapter)})
(hugsql/def-sqlvec-fns "libvtx/db.sql" {:quoting :ansi
                                        :adapter (adapter/kebab-adapter)})


(defn get-tables [db-spec]
  (jdbc/query db-spec ["SELECT name FROM sqlite_master WHERE type='table'"]))
