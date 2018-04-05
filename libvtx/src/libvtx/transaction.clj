(ns libvtx.transaction
  (:require
    [bouncer.core :as bouncer]
    [rop.core :as rop]
    [libvtx.common :refer [->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [transaction-schema]]))


(defn- =validate-params=
  [{:keys [params] :as result}]
  (let [[errors _] (bouncer/validate params transaction-schema)]
    (if (empty? errors)
      (rop/succeed result)
      (rop/fail {:body (:errors errors) :status 400}))))


(defn- =create-transaction=
  [{:keys [params conf] :as result}]
  (db/create-transaction (->db-spec conf) params)
  (-> result 
      (assoc :transaction params)
      (assoc-in [:response :status] 201)))


(defn send-transaction
  [request conf]
  (rop/>>=*
    :transaction
    {:params (:body-params request)
     :conf conf}
    =validate-params=
    (rop/switch =create-transaction=)))
