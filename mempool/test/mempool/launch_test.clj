(ns mempool.launch-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [duct.core :as duct]
    [integrant.core  :as ig]
    [kerodon.test :as kt]
    [peridot.core :as p]
    [mempool.handler.welcome]))


(deftest service-test
  (testing "get response from microservice"
    (let [response (-> (p/session (ig/init-key :mempool.handler/welcome {}))
                       (p/request "/")
                       (kt/has (kt/status? 200)))]
      (is (= (-> response :response :body) "Microservice")))))
