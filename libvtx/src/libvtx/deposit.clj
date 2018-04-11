(ns libvtx.deposit
  (:require
    [clojure.string :refer [trim]]
    [rop.core :as rop]
    [libvtx.balance :refer [read-or-create-balance]]
    [libvtx.common :refer [=validate-params= ->db-spec random-string]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [check-deposit-schema deposit-schema]]
    [libvtx.transaction :refer [send-transaction]]))


(defn- =create-deposit-address=
  [{:keys [params db-spec] :as result}]
  (db/create-deposit-address db-spec params)
  (:address params))


(defn create-deposit-address
  [db-spec token-address receicer-address]
  (rop/>>=
    {:params {:address (random-string 8)
              :token-address token-address
              :receiver-address receicer-address}
     :db-spec db-spec}
    (partial =validate-params= deposit-schema)
    (rop/switch =create-deposit-address=)))


(defn- =check-deposit-status=
  [{:keys [db-spec params] :as result}]
  (let [balance (read-or-create-balance db-spec (:address params) (:token-address params))]
    (> (-> balance :balance trim bigdec) 0)))


(defn check-deposit-status
  [db-spec address token-address]
  (rop/>>=
    {:params {:address address, :token-address token-address}
     :db-spec db-spec}
    (partial =validate-params= check-deposit-schema)
    (rop/switch =check-deposit-status=)))


(defn- =withdraw-tokens=
  [{:keys [db-spec params] :as result}]
  (let [deposit (db/get-deposit-by-address db-spec params)
        balance (:balance (read-or-create-balance db-spec (:address params) (:token-address params)))]
    (send-transaction db-spec 
                      {:from-address (:address params)
                       :to-address (:receiver-address deposit)
                       :amount balance
                       :token-address (:token-address params)})))


(defn withdraw-tokens
  [db-spec address token-address]
  (rop/>>=
    {:params {:address address, :token-address token-address}
     :db-spec db-spec}
    (partial =validate-params= check-deposit-schema)
    (rop/switch =withdraw-tokens=)))
