(ns libvtx.transaction
  (:require
    [clojure.string :refer [trim]]
    [rop.core :as rop]
    [libvtx.balance :refer [read-or-create-balance]]
    [libvtx.common :refer [validate-params ->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [transaction-schema receive-schema]]))


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


(defn- update-balances
  [conf from-balance to-balance from-amount to-amount transaction-amount]
  (let [final-to-amount (str (+ to-amount transaction-amount))
        final-from-amount (str (- from-amount transaction-amount))]
    (db/update-balance (->db-spec conf) (assoc from-balance :balance final-from-amount))
    (db/update-balance (->db-spec conf) (assoc to-balance :balance final-to-amount))))


(defn mempool-transaction
  [conf mempool-interval _]
  (let [mempool-transactions (db/get-mempool-transactions (->db-spec conf) mempool-interval)]
    (doseq [transaction mempool-transactions]
      (let [from-balance (:body (read-or-create-balance {:params {:address (:from_address transaction)
                                                                  :token-address (:token_address transaction)}} 
                                                        conf))
            to-balance (:body (read-or-create-balance {:params {:address (:to_address transaction)
                                                                :token-address (:token_address transaction)}} 
                                                      conf))
            to-amount (-> to-balance :balance trim bigdec)
            from-amount (-> from-balance :balance trim bigdec)
            transaction-amount (-> transaction :amount trim bigdec)]
        (when (>= from-amount transaction-amount)
          (update-balances conf from-balance to-balance from-amount to-amount transaction-amount))
        (db/remove-transaction-from-mempool (->db-spec conf) transaction)))))
