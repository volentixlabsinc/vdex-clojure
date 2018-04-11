(ns libvtx.transaction
  (:require
    [clojure.string :refer [trim]]
    [clj-time.core :as time]
    [clj-time.format :as time-format]
    [rop.core :as rop]
    [libvtx.balance :refer [read-or-create-balance]]
    [libvtx.common :refer [=validate-params= ->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [transaction-confirmation-schema transaction-schema receive-schema]]))


(defn- =create-transaction=
  [{:keys [params db-spec] :as result}]
  (db/create-transaction db-spec params)
  (db/get-transaction-by-params db-spec params))


(defn send-transaction
  [db-spec transaction]
  (rop/>>=
    {:params transaction 
     :db-spec db-spec}
    (partial =validate-params= transaction-schema)
    (rop/switch =create-transaction=)))


(defn- =get-transactions=
  [{:keys [params db-spec] :as result}]
  (db/get-transactions-by-address db-spec params))


(defn receive-transactions
  [db-spec address token-address]
  (rop/>>=
    {:params {:address address, :token-address token-address}
     :db-spec db-spec}
    (partial =validate-params= receive-schema)
    (rop/switch =get-transactions=)))


(defn- update-balances
  [db-spec from-balance to-balance from-amount to-amount transaction-amount]
  (let [final-to-amount (str (+ to-amount transaction-amount))
        final-from-amount (str (- from-amount transaction-amount))]
    (db/update-balance db-spec (assoc from-balance :balance final-from-amount))
    (db/update-balance db-spec (assoc to-balance :balance final-to-amount))))


(defn mempool-transaction
  [db-spec mempool-interval _]
  (let [mempool-transactions (db/get-mempool-transactions db-spec mempool-interval)]
    (doseq [transaction mempool-transactions]
      (let [from-balance  (read-or-create-balance db-spec 
                                                  (:from_address transaction)
                                                  (:token_address transaction))
            to-balance (read-or-create-balance db-spec
                                               (:to_address transaction)
                                               (:token_address transaction))
            to-amount (-> to-balance :balance trim bigdec)
            from-amount (-> from-balance :balance trim bigdec)
            transaction-amount (-> transaction :amount trim bigdec)]
        (when (>= from-amount transaction-amount)
          (update-balances db-spec from-balance to-balance from-amount to-amount transaction-amount))
        (db/remove-transaction-from-mempool db-spec transaction)))))


(defn- =get-transaction-confirmations=
  [{:keys [params db-spec block-time] :as result}]
  (let [confirmed-at (-> db-spec (db/get-transaction-by-id params) first :confirmed-at)]
    (if confirmed-at
      (let [time-formatter (time-format/formatter "yyyy-MM-dd HH:mm:ss")
            parsed-confirmed-at (time-format/parse time-formatter confirmed-at)
            elapsed-seconds (time/in-seconds (time/interval parsed-confirmed-at (time/now)))]
        (->> block-time (/ elapsed-seconds) Math/floor int))
      0)))


(defn transaction-confirmations
  ([db-spec transaction-id]
   (transaction-confirmations db-spec transaction-id 120))
  ([db-spec transaction-id block-time]
   (rop/>>=
     {:params {:transaction-id transaction-id}
      :block-time block-time
      :db-spec db-spec}
     (partial =validate-params= transaction-confirmation-schema)
     (rop/switch =get-transaction-confirmations=))))
