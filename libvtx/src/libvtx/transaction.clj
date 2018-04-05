(ns libvtx.transaction
  (:require
    [bouncer.core :as bouncer]
    [rop.core :as rop]
    [libvtx.common :refer [->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [transaction-schema receive-schema]]))


(defn- validate-params
  [result schema]
  (let [[errors _] (bouncer/validate (:params result) schema)]
    (if (empty? errors)
      (rop/succeed result)
      (rop/fail {:body {:errors errors} :status 400}))))


(defn- =validate-send-params=
  [result]
  (validate-params result transaction-schema))


(defn- =create-transaction=
  [{:keys [params conf] :as result}]
  (db/create-transaction (->db-spec conf) params)
  (let [transaction (db/get-transaction-by-params (->db-spec conf) params)]
    (-> result 
        (assoc :transaction transaction)
        (assoc-in [:response :status] 201))))


(defn send-transaction
  [request conf]
  (rop/>>=*
    :transaction
    {:params (:body-params request)
     :conf conf}
    =validate-send-params=
    (rop/switch =create-transaction=)))


(defn- =validate-receive-params=
  [result]
  (validate-params result receive-schema))


(defn- =get-transactions=
  [{:keys [params conf] :as result}]
  (let [transactions (db/get-transactions-by-address (->db-spec conf) params)]
    (assoc result :transactions transactions)))


(defn receive-transactions
  [request conf]
  (rop/>>=*
    :transactions
    {:params (:params request)
     :conf conf}
    =validate-receive-params=
    (rop/switch =get-transactions=)))
