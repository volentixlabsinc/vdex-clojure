(ns libvtx.transaction
  (:require
    [clojure.string :refer [trim]]
    [clj-time.core :as time]
    [clj-time.format :as time-format]
    [rop.core :as rop]
    [libvtx.balance :refer [read-or-create-balance]]
    [libvtx.common :refer [validate-params ->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [transaction-confirmation-schema transaction-schema receive-schema]]))


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


(defn- =validate-confirmations-params=
  [result]
  (validate-params result transaction-confirmation-schema))


(defn- =get-transaction-confirmations=
  [{:keys [params conf] :as result}]
  (let [confirmed-at (-> conf ->db-spec (db/get-transaction-by-id  params) first :confirmed-at)]
    (if confirmed-at
      (let [time-formatter (time-format/formatter "yyyy-MM-dd HH:mm:ss")
            parsed-confirmed-at (time-format/parse time-formatter confirmed-at)
            elapsed-seconds (time/in-seconds (time/interval parsed-confirmed-at (time/now)))
            confirmations (->> conf :block-time (/ elapsed-seconds) Math/floor int)]
        (assoc result :transaction-confirmations {:transaction-confirmations confirmations}))
      (assoc result :transaction-confirmations {:transaction-confirmations 0}))))


(defn transaction-confirmations
  [request conf]
  (rop/>>=*
    :transaction-confirmations
    {:params (:params request)
     :conf conf}
    =validate-confirmations-params=
    (rop/switch =get-transaction-confirmations=)))
