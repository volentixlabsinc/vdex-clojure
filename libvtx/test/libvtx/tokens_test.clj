(ns libvtx.tokens-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures]]
    [libvtx.common :refer [->db-spec]]
    [libvtx.conf.test-db :refer [create-connection! with-test-db *conf*]]
    [libvtx.db.db :as db]
    [libvtx.token :refer [create-token]]))


(use-fixtures :once create-connection!)
(use-fixtures :each with-test-db)


(def token1 {:address "foo" 
                 :name "BTC"
                 :precision 18})


(def token2 {:address "bar"
                  :name "ETH"
                  :precision "18"
                  :pairs-with ["foo"]})


(deftest balance-test
  (let [db-spec (->db-spec *conf*)]
    (testing "should create new token and new pair"
      (let [_ (db/create-token db-spec token1)
            balance (create-token db-spec token2)]
        (is (= token2 balance))
        (is (= "BTCETH" (:name (db/get-pair-name db-spec {:address (:address token2)
                                                          :pair-address (:address token1)}))))))

    (testing "should return error - missing address"
      (let [balance (create-token db-spec (dissoc token1 :address))]
        (is (= "address must be present" (-> balance :errors :address first)))))
    
    (testing "should return error - missing name"
      (let [balance (create-token db-spec (dissoc token1 :name))]
        (is (= "name must be present" (-> balance :errors :name first)))))
    
    (testing "should return error - missing precision"
      (let [balance (create-token db-spec (dissoc token1 :precision))]
        (is (= "precision must be present" (-> balance :errors :precision first)))))))
