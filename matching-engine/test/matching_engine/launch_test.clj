(ns matching-engine.launch-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [duct.core :as duct]
    [integrant.core  :as ig]
    [kerodon.test :as kt]
    [peridot.core :as p]
    [matching-engine.handler.welcome]))


(deftest service-test
  (testing "get response from microservice"
    (let [response (-> (p/session (ig/init-key :matching-engine.handler/welcome {}))
                       (p/request "/")
                       (kt/has (kt/status? 200)))]
      (is (= (-> response :response :body) "Microservice")))))
