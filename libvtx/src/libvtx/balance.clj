(ns libvtx.balance
  (:require
    [rop.core :as rop]
    [libvtx.common :refer [=validate-params= ->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [receive-schema]]))


(defn- =get-or-create-balance=
  [{:keys [params db-spec] :as result}]
  (let [balance (db/get-balance-by-address db-spec params)]
    (if (empty? balance)
      (let [new-balance (assoc params :balance "0")]
        (db/create-balance db-spec new-balance)
        new-balance) 
      (first balance))))


(defn read-or-create-balance
  [db-spec address token-address]
  (rop/>>=
    {:params {:address address, :token-address token-address}
     :db-spec db-spec}
    (partial =validate-params= receive-schema)
    (rop/switch =get-or-create-balance=)))
