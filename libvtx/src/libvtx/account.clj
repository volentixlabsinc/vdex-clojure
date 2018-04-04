(ns libvtx.account
  (:require 
    [libvtx.crypto :as crypto]
    [libvtx.db :as db]))

(defn get-all [db-spec]
  (db/get-accounts db-spec))

(defn create [db-spec]
  (let [address (crypto/gen-address)]
    (db/create-account db-spec
                       {:address address})
    address))

(defn get [db-spec address]
  (db/get-account db-spec
                  {:address address}))


