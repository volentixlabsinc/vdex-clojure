(ns libvtx.token
  (:require 
    [libvtx.crypto :as crypto]
    [libvtx.db :as db]))

(defn get-all
  "Get all tokens"
  [db-spec]
  (db/get-tokens db-spec))

(defn create
  "Create token for specified token name"
  [db-spec token-name]
  (let [address (crypto/gen-address)]
    (db/create-token db-spec
                     {:name token-name
                      :address address})
    address))

(defn get
  "Get token by address"
  [db-spec address]
  (db/get-token db-spec
                {:address address}))

