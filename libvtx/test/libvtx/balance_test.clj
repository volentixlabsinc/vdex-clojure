(ns libvtx.balance-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures]]
    [libvtx.balance :refer [read-or-create-balance]]
    [libvtx.common :refer [->db-spec]]
    [libvtx.conf.test-db :refer [create-connection! with-test-db *conf*]]
    [libvtx.db.db :as db]))


(use-fixtures :once create-connection!)
(use-fixtures :each with-test-db)


(def test-balance {:address "foo" 
                   :token-address "fake-address"})


(deftest balance-test
  (let [db-spec (->db-spec *conf*)]
    (testing "should create new balance"
      (let [balance (read-or-create-balance db-spec (:address test-balance) (:token-address test-balance))]
        (is (= (assoc test-balance :balance "0") balance))))

    (testing "should read a balance"
      (let [_ (db/update-balance db-spec (assoc test-balance :balance "10"))
            balance (read-or-create-balance db-spec (:address test-balance) (:token-address test-balance))]
        (is (= (assoc test-balance :balance "10") balance))))

    (testing "should return error - empty address"
      (let [balance (read-or-create-balance db-spec nil (:token-address test-balance))]
        (is (= "address must be present" (-> balance :errors :address first)))))))
