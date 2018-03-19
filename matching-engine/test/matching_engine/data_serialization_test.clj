(ns matching-engine.data-serialization-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.io :as io]
   [clojure.core.async :as async]
   [integrant.core  :as ig]
   [matching-engine.store :as store]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

;; we dacide later about data shape, needs mode research
(defn gen-order []
  {:account-id (uuid)
   :order-id (uuid)
   :market "BTC-SHLD"
   :type "market"
   :quantity (bigdec 1)
   :limit (bigdec 1)
   :price (bigdec 1)})

(def order-book
  {:asks (gen-order)
   :bids (gen-order)})

(deftest binary-format-serialization-test
  (let [f-name   "order-book.data"
        file-store (ig/init-key :matching-engine.store/file {:dir-path ".store/"})
        read-ch (async/chan)]
    (testing "save data in binary fressian format with Java NIO"
      (is (true? (store/save! file-store
                              (store/->order-book order-book)
                              f-name))))

    (testing "read data from binary file saved used fressian format using Java NIO"
      (is (= order-book
             (store/load! file-store :order-book f-name))))))
