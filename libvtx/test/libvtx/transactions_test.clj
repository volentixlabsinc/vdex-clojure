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
    [libvtx.handler.api]))


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
                         (kt/has (kt/status? 400)))]))))
