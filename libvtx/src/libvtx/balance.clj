(ns libvtx.balance
  (:require
    [libvtx.crypto :as crypto]
    [libvtx.db     :as db]))

(defn get-all [db-spec]
  (db/get-balances db-spec))

(defn create [db-spec address token-address]
  (db/create-balance db-spec
                     {:address address
                      :token-address token-address}))

(defn get-by-account-token
  "Get account balance for specified token owned by this account"
  [db-spec address token-address]
  (db/get-account-token-balance db-spec
                                {:address address
                                 :token-address token-address}))

(defn get-by-account
  "Get balance for specified account. It will contain balances for all tokens owned by this account"
  [db-spec address]
  (db/get-account-balance db-spec {:address address}))
