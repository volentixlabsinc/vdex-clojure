(ns libvtx.tokens-test
  (:require
   [clojure.test :refer :all]
   [libvtx.common :refer [->db-spec]]
   [libvtx.conf.test-db :refer [create-connection! with-test-db *conf*]]
   [libvtx.token :as token]))


(use-fixtures :once create-connection!)
(use-fixtures :each with-test-db)


(deftest tokens-test
  (let [db-spec (->db-spec *conf*)
        address (token/create db-spec "ETH")]
    (is (= address
           (:address (token/get db-spec address))))))
