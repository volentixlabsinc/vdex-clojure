(ns mempool.launch-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.io :as io]
   [clj-http.client :as http]
   [duct.core       :as duct]
   [duct.logger     :as logger]
   [integrant.core  :as ig]
   [kerodon.test :as kt]
   [peridot.core :as p]))

(duct/load-hierarchy)

(def base-config (-> "mempool/config.edn"
                     io/resource
                     duct/read-config))

(deftest configuration-test
  (testing "start/stop microservice"
    (let [system (-> base-config
                     (assoc :duct.core/environment :development)
                     duct/prep
                     ig/init)]
      (try
        (let [resp (http/get "http://127.0.0.1:5000/")]
          (is (= (:status resp) 200))
          (is (= (:body resp) "Microservice")))
        (finally
          (ig/halt! system))))))

(deftest service-test
  (testing "get response from microservice"
    (let [response (-> (p/session (ig/init-key :mempool.handler/welcome {}))
                       (p/request "/")
                       (kt/has (kt/status? 200)))]
      (is (= (-> response :response :body) "Microservice")))))
