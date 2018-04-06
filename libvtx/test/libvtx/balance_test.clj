(ns libvtx.balance-test
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


(def balance {:address "foo" 
              :token-address "fake-address"})


(deftest balance-test
  (let [conf (merge api-config *conf*)]
    (testing "should create new balance"
      (let [response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/balance" 
                                    :params balance)
                         (kt/has (kt/status? 200)))]
        (is (= (assoc balance :balance "0") (-> response :response :body str->kwjson)))))

    (testing "should read a balance"
      (let [_ (db/update-balance (->db-spec conf) (assoc balance :balance "10"))
            response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/balance" 
                                    :params balance)
                         (kt/has (kt/status? 200)))]
        (is (= (assoc balance :balance "10") (-> response :response :body str->kwjson)))))

    (testing "should return 400 - missing address"
      (let [response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/balance")
                         (kt/has (kt/status? 400)))]))))
