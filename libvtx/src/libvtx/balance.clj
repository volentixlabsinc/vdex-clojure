(ns libvtx.balance
  (:require
    [rop.core :as rop]
    [libvtx.common :refer [validate-params ->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [receive-schema]]))


(defn- =validate-balance-params=
  [result]
  (validate-params result receive-schema))


(defn- =get-or-create-balance=
  [{:keys [params conf] :as result}]
  (let [balance (db/get-balance-by-address (->db-spec conf) params)]
    (if (empty? balance)
      (let [new-balance (assoc params :balance "0")]
        (db/create-balance (->db-spec conf) new-balance)
        (assoc result :balance new-balance)) 
      (assoc result :balance (first balance)))))


(defn read-or-create-balance
  [request conf]
  (rop/>>=*
    :balance
    {:params (:params request)
     :conf conf}
    =validate-balance-params=
    (rop/switch =get-or-create-balance=)))
