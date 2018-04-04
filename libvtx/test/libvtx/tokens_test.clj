(ns libvtx.tokens-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.io   :as io]
   [clojure.java.jdbc :as jdbc]
   [duct.core         :as duct]
   [duct.logger       :as logger]
   [integrant.core    :as ig]
   [fipp.edn :refer [pprint]]
   [libvtx.token   :as token]
   [libvtx.account :as account]
   [libvtx.balance :as balance]))

(duct/load-hierarchy)

(defn system []
  (-> "libvtx/config.edn"
      io/resource
      duct/read-config
      duct/prep
      ig/init))

(defn ->db-spec [system]
  (-> system :duct.database.sql/hikaricp :spec))

(deftest abstraction-test
  (let [sys (system)
        db-spec (->db-spec sys)
        token-address-eth (token/create db-spec "ETH")
        token-address-btc (token/create db-spec "BTC")
        account-address-1 (account/create db-spec)
        account-address-2 (account/create db-spec)]

    (testing "token has address"
      (is (= token-address-eth
             (:address (token/get db-spec token-address-eth)))))

    (testing "each token has unique address"
      (is (not= token-address-eth token-address-btc)))

    (testing "account has address"
      (is (= account-address-1
             (:address (account/get db-spec account-address-1)))))

    (testing "each account has unique address"
      (is (not= account-address-1 account-address-2)))

    (testing "balances -> "
      (let [_ (balance/create db-spec account-address-1 token-address-eth)
            _ (balance/create db-spec account-address-1 token-address-btc)
            account-balances (balance/get-by-account db-spec account-address-1)
            account-token-balance (balance/get-by-account-token db-spec
                                                                account-address-1
                                                                token-address-eth)]

        (testing "account may have many balances (for each token they owned)"
          (is (= 2
                 (count account-balances))))

        (testing "each token T owned by account A has only one balance"
          (is (= account-address-1
                 (:address account-token-balance)))
          (is (= token-address-eth
                 (:token-address account-token-balance))))))

    (ig/halt! sys)))
