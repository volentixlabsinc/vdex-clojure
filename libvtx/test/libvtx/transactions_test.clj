(ns libvtx.transactions-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures]]
    [libvtx.common :refer [->db-spec]]
    [libvtx.conf.test-db :refer [create-connection! with-test-db *conf*]]
    [libvtx.db.db :as db]
    [libvtx.transaction :refer [transaction-confirmations send-transaction receive-transactions mempool-transaction]]))


(use-fixtures :once create-connection!)
(use-fixtures :each with-test-db)


(def test-transaction {:from-address "foo" 
                       :to-address "bar" 
                       :amount "10" 
                       :token-address "fake-address"
                       :message "message"})


(deftest transactions-test
  (let [db-spec (->db-spec *conf*)]
    (testing "should create and return new transaction"
      (let [transaction (send-transaction db-spec test-transaction)]
        (is (= (select-keys transaction (keys test-transaction))
               test-transaction))
        (is (= 1 (:mempool transaction)))
        (is (nil? (:confirmed-at transaction)))))

    (testing "should return error - missing amount"
      (let [transaction (send-transaction db-spec (dissoc test-transaction :amount))]
        (is (= "amount must be present" (-> transaction :errors :amount first)))))

    (testing "should return 4 errors - missing all mandatory fields"
      (let [transaction (send-transaction db-spec nil)]
        (is (= 4 (-> transaction :errors count)))))

    (testing "should return 2 transactions from 3 available based on the same address"
      (let [_ (db/create-transaction db-spec test-transaction)
            _ (db/create-transaction db-spec (assoc test-transaction :to-address "baz"))
            transactions (receive-transactions db-spec 
                                               (:to-address test-transaction)
                                               (:token-address test-transaction))]
        (is (= 2 (count transactions)))
        (is (= 2 (count (filter #(= (:to-address %) (:to-address test-transaction)) transactions))))
        (is (= 2 (count (filter #(= (:token-address %) (:token-address test-transaction)) transactions))))))

    (testing "should return error - address missing"
      (let [transactions (receive-transactions db-spec nil nil)]
        (is (= "address must be present" (-> transactions :errors :address first)))))

    (testing "should transfer balance and remove transaction from mempool"
      (let [_ (db/create-balance db-spec {:address "address1" 
                                          :token-address "taddress" 
                                          :balance "15"})
            _ (db/create-balance db-spec {:address "address2" 
                                          :token-address "taddress" 
                                          :balance "0"})
            _ (db/create-transaction db-spec {:from-address "address1"
                                              :to-address "address2"
                                              :amount "10"
                                              :token-address "taddress"
                                              :created-at "2007-01-01 10:00:00"})
            _ (mempool-transaction db-spec 0 nil)
            balance1 (db/get-balance-by-address db-spec {:address "address1" 
                                                         :token-address "taddress"})
            balance2 (db/get-balance-by-address db-spec {:address "address2" 
                                                         :token-address "taddress"})
            transaction (db/get-transactions-by-address db-spec {:address "address2" 
                                                                 :token-address "taddress"})]
        (is (= "5" (-> balance1 first :balance)))
        (is (= "10" (-> balance2 first :balance)))
        (is (-> transaction first :confirmed-at nil? not))
        (is (= 0 (-> transaction first :mempool)))))

    (testing "should remove transaction from mempool and not transfer balance, insufficient amount"
      (let [_ (db/create-transaction db-spec {:from-address "address1"
                                              :to-address "address2"
                                              :amount "10"
                                              :token-address "taddress"
                                              :created-at "2007-01-01 10:00:00"})
            _ (mempool-transaction db-spec 0 nil)
            balance1 (db/get-balance-by-address db-spec {:address "address1" 
                                                         :token-address "taddress"})
            balance2 (db/get-balance-by-address db-spec {:address "address2" 
                                                         :token-address "taddress"})
            transaction (db/get-transactions-by-address db-spec {:address "address2" 
                                                                 :token-address "taddress"})]
        (is (= "5" (-> balance1 first :balance)))
        (is (= "10" (-> balance2 first :balance)))
        (is (-> transaction first :confirmed-at nil? not))
        (is (= 0 (-> transaction first :mempool)))))

    (testing "should return 0 confirmations - transaction not confirmed"
      (let [_ (db/create-transaction db-spec {:from-address "address1"
                                              :to-address "address2"
                                              :amount "1"
                                              :token-address "taddress"
                                              :created-at "2007-01-01 10:00:00"})
            transactions (db/get-transactions-by-address db-spec {:address "address2"
                                                                  :token-address "taddress"})
            transaction-id (->> transactions (filter #(= "1" (:amount %))) first :id)
            confirmations (transaction-confirmations db-spec transaction-id 1)]
        (is (= 0 confirmations))))

    (testing "should return some confirmations"
      (let [_ (mempool-transaction db-spec 0 nil) 
            transactions (db/get-transactions-by-address db-spec {:address "address2"
                                                                  :token-address "taddress"})
            transaction-id (->> transactions (filter #(= "1" (:amount %))) first :id)
            _ (Thread/sleep 1000)
            confirmations (transaction-confirmations db-spec transaction-id 1)]
        (is (< 0 confirmations))))

    (testing "should return error - transaction id missing"
      (let [confirmations (transaction-confirmations db-spec nil)]
        (is (= "transaction-id must be present" (-> confirmations :errors :transaction-id first)))))))
