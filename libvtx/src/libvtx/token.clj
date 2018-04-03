(ns libvtx.token
(:require 
   [libvtx.crypto :as crypto]
   [libvtx.db :as db]))

(defn get-all [db-spec]
  (db/get-tokens db-spec))

(defn create [db-spec token-name]
  (let [address (crypto/gen-address)]
    (db/create-token db-spec
                     {:name token-name
                      :address address})
    address))

(defn get [db-spec address]
  (db/get-token db-spec
                {:address address}))

