(ns libvtx.deposit-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures]]
    [libvtx.balance :refer [read-or-create-balance]]
    [libvtx.common :refer [->db-spec]]
    [libvtx.conf.test-db :refer [create-connection! with-test-db *conf*]]
    [libvtx.db.db :as db]
    [libvtx.deposit :refer [create-deposit-address check-deposit-status withdraw-tokens]]))


(use-fixtures :once create-connection!)
(use-fixtures :each with-test-db)


(def test-deposit {:receiver-address "foo" 
                   :token-address "fake-address"})


(deftest deposit-test
  (let [db-spec (->db-spec *conf*)
        new-deposit-address (create-deposit-address db-spec "token-address" "receiver-address")]
    (testing "new deposit address should be 8 digits random string"
      (is (= 8 (count new-deposit-address))))

    (testing "should return error - missing receiver address"
      (let [deposit (create-deposit-address db-spec "token-address" nil)]
        (is (= "receiver-address must be present" (-> deposit :errors :receiver-address first)))))

    (testing "should return error - missing token address"
      (let [deposit (create-deposit-address db-spec nil "receiver-address")]
        (is (= "token-address must be present" (-> deposit :errors :token-address first)))))

    (testing "should check deposit status and return false"
      (let [_ (read-or-create-balance db-spec new-deposit-address "token-address")
            deposit-status (check-deposit-status db-spec new-deposit-address "token-address")]
        (is (= false deposit-status))))

    (testing "should check deposit status and return true"
      (let [_ (db/update-balance db-spec {:address new-deposit-address
                                          :token-address "token-address"
                                          :balance "10"})
            deposit-status (check-deposit-status db-spec new-deposit-address "token-address")]
        (is (= true deposit-status))))

    (testing "should return error - missing address"
      (let [deposit-status (check-deposit-status db-spec nil "token-address")]
        (is (= "address must be present" (-> deposit-status :errors :address first)))))

    (testing "should return error - missing token address"
      (let [deposit-status (check-deposit-status db-spec new-deposit-address nil)]
        (is (= "token-address must be present" (-> deposit-status :errors :token-address first)))))

    (testing "should withdraw tokens"
      (let [transaction (withdraw-tokens db-spec new-deposit-address "token-address")]
        (is (= "10" (:amount transaction)))
        (is (= "token-address" (:token-address transaction)))
        (is (= new-deposit-address (:from-address transaction)))
        (is (= "receiver-address" (:to-address transaction)))
        (is (= 1 (:mempool transaction)))
        (is (nil? (:confirmed-at transaction)))))

    (testing "should return error - missing address"
      (let [transaction (withdraw-tokens db-spec nil "token-address")]
        (is (= "address must be present" (-> transaction :errors :address first)))))

    (testing "should return error - missing token address"
      (let [transaction (withdraw-tokens db-spec new-deposit-address nil)]
        (is (= "token-address must be present" (-> transaction :errors :token-address first)))))))
