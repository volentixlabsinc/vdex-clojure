(ns matching-engine.matching-orders-and-reference-price-test
  (:require
   [clojure.test :refer :all]
   [matching-engine.order     :as order]
   [matching-engine.orderbook :as orderbook]
   [matching-engine.trade     :as trade]
   [matching-engine.engine    :as engine]))

(def order-a
  {:from-address "A"
   :market "BTCETH"
   :side -1
   :amount 100
   :price nil})

(def order-b
  {:from-address "B"
   :market "BTCETH"
   :side -1
   :amount 200
   :price nil})

(deftest matching-and-use-reference-price-test
  (testing "case 1"

    ;; Scenario: Updating reference price as it is set at the opening and trades occur
    ;;   Given the reference price is set to "10"
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | 11    |
    ;;   | B    | Sell | 100    | 11    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | B      | 100    | 11    |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   And the reference price is reported as "11"
    (testing "case 1"
      (let [engine (-> (engine/init "BTCETH" 10)
                       (engine/accept-order (assoc order-a :price 11 :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-b :price 11 :amount 100 :side order/SELL)))
            [trade-a] (:trades engine)]
        ;; new reference price
        (is (= 11 (:reference-price engine)))
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "B",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 11})
                     trade-a))
        ;; rest orders
        (is (-> engine :buy orderbook/orders empty?))
        (is (-> engine :sell orderbook/orders empty?))))

    ;; Scenario: Updating reference price as it is set at the opening and trades occur
    ;;   Given the reference price is set to "10"
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | 12    |
    ;;   | B    | Sell | 100    | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | B      | 100    | 12    |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   And the reference price is reported as "12"
    (testing "case 1"
      (let [engine (-> (engine/init "BTCETH" 10)
                       (engine/accept-order (assoc order-a :price 12 :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-b :price :mp :amount 100 :side order/SELL)))
            [trade-a] (:trades engine)]
        ;; new reference price
        (is (= 12 (:reference-price engine)))
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "B",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 12})
                     trade-a))
        ;; rest orders
        (is (-> engine :buy orderbook/orders empty?))
        (is (-> engine :sell orderbook/orders empty?))))


    ;; Scenario: Updating reference price as it is set at the opening and trades occur
    ;;   Given the reference price is set to "10"
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | MO    |
    ;;   | B    | Sell | 100    | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | B      | 100    | 10    | reference price used
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   And the reference price is reported as "10"
    (testing "case 1"
      (let [engine (-> (engine/init "BTCETH" 10)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-b :price :mp :amount 100 :side order/SELL)))
            [trade-a] (:trades engine)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "B",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10})
                     trade-a))
        ;; rest orders
        (is (-> engine :buy orderbook/orders empty?))
        (is (-> engine :sell orderbook/orders empty?))))))
