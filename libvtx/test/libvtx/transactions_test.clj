(ns libvtx.transactions-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures]]
    [integrant.core  :as ig]
    [kerodon.test :as kt]
    [peridot.core :as p]
    [libvtx.common :refer [str->kwjson ->db-spec]]
    [libvtx.conf.common :refer [api-config]]
    [libvtx.conf.test-db :refer [create-connection! with-test-db *conf*]]
    [libvtx.db.db :as db]
    [libvtx.handler.api]
    [libvtx.transaction :refer [mempool-transaction]]))


(use-fixtures :once create-connection!)
(use-fixtures :each with-test-db)


(def transaction {:from-address "foo" 
                  :to-address "bar" 
                  :amount "10" 
                  :token-address "fake-address"
                  :message "message"})


(deftest transactions-test
  (let [conf (merge api-config *conf*)]
    (testing "should create and return new transaction"
      (let [response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/transactions/send" 
                                    :request-method :post
                                    :content-type "application/json"
                                    :body-params transaction)
                         (kt/has (kt/status? 201)))]
        (is (= (-> response :response :body str->kwjson (select-keys (keys transaction)))
               transaction))))

    (testing "should return 400 - missing amount"
      (let [response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/transactions/send" 
                                    :request-method :post
                                    :content-type "application/json"
                                    :body-params (dissoc transaction :amount))
                         (kt/has (kt/status? 400)))]))

    (testing "should return 2 transactions from 3 available based on the same address"
      (let [_ (db/create-transaction (->db-spec conf) (assoc transaction :token-address "another-address"))
            _ (db/create-transaction (->db-spec conf) (assoc transaction :to-address "baz"))
            response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/transactions/receive" 
                                    :params {:address (:to-address transaction)})
                         (kt/has (kt/status? 200)))]
        (is (= 2 (-> response :response :body str->kwjson count)))))
    
    (testing "should return only 1 transaction filtered by token address"
      (let [response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/transactions/receive" 
                                    :params {:address (:to-address transaction)
                                             :token-address "another-address"})
                         (kt/has (kt/status? 200)))]
        (is (= 1 (-> response :response :body str->kwjson count)))))

    (testing "should return 400 - missing address"
      (let [response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/transactions/receive")
                         (kt/has (kt/status? 400)))]))
    
    (testing "should transfer balance and remove transaction from mempool"
      (let [_ (db/create-balance (->db-spec conf) {:address "address1" 
                                                   :token-address "taddress" 
                                                   :balance "15"})
            _ (db/create-balance (->db-spec conf) {:address "address2" 
                                                   :token-address "taddress" 
                                                   :balance "0"})
            _ (db/create-transaction (->db-spec conf) {:from-address "address1"
                                                       :to-address "address2"
                                                       :amount "10"
                                                       :token-address "taddress"
                                                       :created-at "2007-01-01 10:00:00"})
            _ (mempool-transaction conf 0 nil)
            balance1 (db/get-balance-by-address (->db-spec conf) {:address "address1" 
                                                                  :token-address "taddress"})
            balance2 (db/get-balance-by-address (->db-spec conf) {:address "address2" 
                                                                  :token-address "taddress"})
            transaction (db/get-transactions-by-address (->db-spec conf) {:address "address2" 
                                                                          :token-address "taddress"})]
        (is (= "5" (-> balance1 first :balance)))
        (is (= "10" (-> balance2 first :balance)))
        (is (-> transaction first :confirmed-at nil? not))
        (is (= 0 (-> transaction first :mempool)))))
    
    (testing "should remove transaction from mempool and not transfer balance, insufficient amount"
      (let [_ (db/create-transaction (->db-spec conf) {:from-address "address1"
                                                       :to-address "address2"
                                                       :amount "10"
                                                       :token-address "taddress"
                                                       :created-at "2007-01-01 10:00:00"})
            _ (mempool-transaction conf 0 nil)
            balance1 (db/get-balance-by-address (->db-spec conf) {:address "address1" 
                                                                  :token-address "taddress"})
            balance2 (db/get-balance-by-address (->db-spec conf) {:address "address2" 
                                                                  :token-address "taddress"})
            transaction (db/get-transactions-by-address (->db-spec conf) {:address "address2" 
                                                                          :token-address "taddress"})]
        (is (= "5" (-> balance1 first :balance)))
        (is (= "10" (-> balance2 first :balance)))
        (is (-> transaction first :confirmed-at nil? not))
        (is (= 0 (-> transaction first :mempool)))))))
