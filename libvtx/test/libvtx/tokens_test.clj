(ns libvtx.tokens-test
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


(def token {:address "foo" 
            :name "BTC"
            :precision 18})


(def token-for-request {:address "bar"
                        :name "ETH"
                        :precision "18"
                        :pairs-with ["foo"]})


(deftest balance-test
  (let [conf (merge api-config *conf*)]
    (testing "should create new token and new pair"
      (let [_ (db/create-token (->db-spec conf) token)
            response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/token" 
                                    :request-method :post
                                    :content-type "application/json"
                                    :body-params token-for-request)
                         (kt/has (kt/status? 201)))]
        (is (= token-for-request (-> response :response :body str->kwjson)))
        (is (= "BTCETH" (:name (db/get-pair-name (->db-spec conf) {:address (:address token-for-request)
                                                                   :pair-address (:address token)}))))))

    (testing "should return 400 - missing params"
      (let [response (-> (p/session (ig/init-key :libvtx.handler/api conf))
                         (p/request "/token"
                                    :request-method :post
                                    :content-type "application/json")
                         (kt/has (kt/status? 400)))]))))
