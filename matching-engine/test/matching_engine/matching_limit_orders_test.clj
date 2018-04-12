(ns matching-engine.matching-limit-orders-test
  (:require
   [clojure.test :refer :all]
   [matching-engine.order     :as order]
   [matching-engine.orderbook :as orderbook]
   [matching-engine.trade     :as trade]
   [matching-engine.engine    :as engine]))

(def order-a
  {:from-address "A"
   :market "BTCETH"
   :side order/BUY
   :amount 100
   :price 10.4})

(def order-b
  {:from-address "B"
   :market "BTCETH"
   :side order/BUY
   :amount 200
   :price 10.3})

(def order-c
  {:from-address "C"
   :market "BTCETH"
   :side order/SELL
   :amount 100
   :price 10.7})

(def order-d
  {:from-address "D"
   :market "BTCETH"
   :side order/SELL
   :amount 200
   :price 10.8})

(def order-e
  {:from-address "E"
   :market "BTCETH"
   :side -1
   :amount 100
   :price nil})

(def engine-state (atom nil))

(deftest matching-limit-orders-test
  (testing "matchiing limit orders"

    ;; If submit initial non-crossing orders to work with
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | 10.4  |
    ;;   | B    | Buy  | 200    | 10.3  |
    ;;   | C    | Sell | 100    | 10.7  |
    ;;   | D    | Sell | 200    | 10.8  |
    ;;   Then no trades are generated
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | AMount | Price | Price | Amount | Addr |
    ;;   | A    | 100    | 10.4  | 10.7  | 100    | C    |
    ;;   | B    | 200    | 10.3  | 10.8  | 200    | D    |
    (testing "non crossing orders in orderbook"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order order-a)
                       (engine/accept-order order-b)
                       (engine/accept-order order-c)
                       (engine/accept-order order-d))
            [a b] (-> engine :buy orderbook/orders)
            [c d] (-> engine :sell orderbook/orders)
            _ (reset! engine-state engine)]
        ;; no trades
        (is (-> engine :trades empty?))
        ;; orders
        (is (.equals a order-a))
        (is (.equals b order-b))
        (is (.equals c order-c))
        (is (.equals d order-d))))

    ;; Scenario: Matching a single Buy order against identical in quantity outstanding Sell order
    ;;   When we have previous test state (no crossing orders)
    ;;   When the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price             |
    ;;   | E    | Buy  | 100    | <buy-price>       |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price                  |
    ;;   | E     | C      | 100    | <expected-trade-price> |
    ;;   And market order book looks like:
    ;;   |---- buy(bids) ----------| ---- sell(asks) --------|
    ;;   | Addr   | Amount | Price | Price | Amount | Addr   |
    ;;   | A      | 100    | 10.4  | 10.8  | 200    | D      |
    ;;   | B      | 200    | 10.3  |       |        |        |
    ;;   Examples:
    ;;   | Buy Order Limit | Expected Trade Price | Comments                                       |
    ;;   | 10.7            | 10.7                 | Exact same price as top of the Sell order book |
    ;;   | 10.8            | 10.7                 | Higher price then the top of the Sell book     |
    (testing "match buy->sell (drain both)"
      (doseq [[buy-price expected-trade-price] [[10.7 10.7]
                                                [10.8 10.7]]]
        (let [engine (engine/accept-order @engine-state
                                          (assoc order-e :price buy-price :side order/BUY))
              trade (-> engine :trades last)
              [a b] (-> engine :buy orderbook/orders)
              [d] (-> engine :sell orderbook/orders)]
          ;; trade
          (is (= 1 (-> engine :trades count)))
          (is (.equals (trade/map->Trade {:buyer-address "E",
                                          :seller-address "C",
                                          :market "BTCETH",
                                          :amount 100,
                                          :price expected-trade-price})
                       trade))
          ;; rest orders
          (is (= 2 (-> engine :buy orderbook/orders count)))
          (is (= 1 (-> engine :sell orderbook/orders count)))
          (is (.equals a order-a))
          (is (.equals b order-b))
          (is (.equals d order-d)))))

    ;; Scenario: Matching a single Sell order against identical in quantity outstanding Buy order
    ;;   When we have previous test state (no crossing orders)
    ;;   When the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price             |
    ;;   | E    | Sell | 100    | <sell-price>      |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price                  |
    ;;   | A     | A      | 100    | <expected-trade-price> |
    ;;   And market order book looks like:
    ;;   |---- buy(bids) ----------| ---- sell(asks) --------|
    ;;   | Addr   | Amount | Price | Price | Amount | Addr   |
    ;;   | B      | 200    | 10.3  | 10.7  | 100    | C      |
    ;;   |        |        |       | 10.8  | 200    | D      |
    ;;   Examples:
    ;;   | Sell Order Limit | Expected Trade Price | Comments                                      |
    ;;   | 10.4             | 10.4                 | Exact same price as top of the Buy order book |
    ;;   | 10.3             | 10.4                 | Lower price then the top of the Buy book      |
    (testing "match sell->buy (drain both)"
      (doseq [[sell-price expected-trade-price] [[10.4 10.4]
                                                 [10.3 10.4]]]
        (let [engine (engine/accept-order @engine-state
                                          (assoc order-e :price sell-price :side order/SELL))
              trade (-> engine :trades last)
              [b] (-> engine :buy orderbook/orders)
              [c d] (-> engine :sell orderbook/orders)]
          ;; trade
          (is (= 1 (-> engine :trades count)))
          (is (.equals (trade/map->Trade {:buyer-address "A",
                                          :seller-address "E",
                                          :market "BTCETH",
                                          :amount 100,
                                          :price expected-trade-price})
                       trade))
          ;; rest orders
          (is (= 1 (-> engine :buy orderbook/orders count)))
          (is (= 2 (-> engine :sell orderbook/orders count)))
          (is (.equals b order-b))
          (is (.equals c order-c))
          (is (.equals d order-d)))))

    ;; Scenario: Matching a Buy order large enough to clear the Sell book
    ;;   When we have previous test state (no crossing orders)
    ;;   When the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | E    | Buy  | 350    | 10.8  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | E     | C      | 100    | 10.7  |
    ;;   | E     | D      | 200    | 10.8  |
    ;;   And market order book looks like:
    ;;   |---- buy(bids) ----------| ---- sell(asks) --------|
    ;;   | Addr   | Amount | Price | Price | Amount | Addr   |
    ;;   | E      | 50     | 10.8  |       |        |        |
    ;;   | A      | 100    | 10.4  |       |        |        |
    ;;   | B      | 200    | 10.3  |       |        |        |
    (testing "match buy->sell (drain all sell orders)"
      (let [engine (engine/accept-order @engine-state
                                        (assoc order-e
                                               :price 10.8
                                               :amount 350
                                               :side order/BUY))
            [trade-c trade-d] (:trades engine)
            [e a b] (-> engine :buy orderbook/orders)]
        ;; trade
        (is (= 2 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "E",
                                        :seller-address "C",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.7})
                      trade-c))
        (is (.equals (trade/map->Trade {:buyer-address "E",
                                        :seller-address "D",
                                        :market "BTCETH",
                                        :amount 200,
                                        :price 10.8})
                      trade-d))
        ;; rest orders
        (is (= 3 (-> engine :buy orderbook/orders count)))
        (is (-> engine :sell orderbook/orders empty?))
        (is (= ["E" 50 10.8] ((juxt :from-address :amount :price) e)))
        (is (.equals a order-a))
        (is (.equals b order-b))))

    ;; Scenario: Matching a Sell order large enough to clear the Buy book
    ;;   When we have previous test state (no crossing orders)
    ;;   When the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | E    | Sell | 350    | 10.3  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | E      | 100    | 10.4  |
    ;;   | B     | E      | 200    | 10.3  |
    ;;   And market order book looks like:
    ;;   |---- buy(bids) ----------| ---- sell(asks) --------|
    ;;   | Addr   | Amount | Price | Price | Amount | Addr   |
    ;;   |        |        |       | 10.3  | 50     | E      |
    ;;   |        |        |       | 10.7  | 100    | C      |
    ;;   |        |        |       | 10.8  | 200    | D      |
    (testing "match sell->buy (drain all buy orders)"
        (let [engine (engine/accept-order @engine-state
                                          (assoc order-e
                                                 :price 10.3
                                                 :amount 350
                                                 :side order/SELL))
              [trade-a trade-b] (:trades engine)
              [e c d] (-> engine :sell orderbook/orders)]
          ;; trade
          (is (= 2 (-> engine :trades count)))
          (is (.equals (trade/map->Trade {:buyer-address "A",
                                          :seller-address "E",
                                          :market "BTCETH",
                                          :amount 100,
                                          :price 10.4})
                       trade-a))
          (is (.equals (trade/map->Trade {:buyer-address "B",
                                          :seller-address "E",
                                          :market "BTCETH",
                                          :amount 200,
                                          :price 10.3})
                       trade-b))
          ;; rest orders
          (is (-> engine :buy orderbook/orders empty?))
          (is (= 3 (-> engine :sell orderbook/orders count)))
          (is (= ["E" 50 10.3] ((juxt :from-address :amount :price) e)))
          (is (.equals c order-c))
          (is (.equals d order-d))))

    ;; Scenario: Matching a large Buy order partially
    ;;   When we have previous test state (no crossing orders)
    ;;   When the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | E    | Buy  | 350    | 10.7  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | E     | C      | 100    | 10.7  |
    ;;   And market order book looks like:
    ;;   |---- buy(bids) ----------| ---- sell(asks) --------|
    ;;   | Addr   | Amount | Price | Price | Amount | Addr   |
    ;;   | E      | 250    | 10.8  | 10.8  | 200    | D      |
    ;;   | A      | 100    | 10.4  |       |        |        |
    ;;   | B      | 200    | 10.3  |       |        |        |
    (testing "match buy->sell partially"
      (let [engine (engine/accept-order @engine-state
                                        (assoc order-e
                                               :price 10.7
                                               :amount 350
                                               :side order/BUY))
            [trade-c] (:trades engine)
            [e a b] (-> engine :buy orderbook/orders)
            [d] (-> engine :sell orderbook/orders)]
        ;; trade
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "E",
                                        :seller-address "C",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.7})
                      trade-c))
        ;; rest orders
        (is (= 3 (-> engine :buy orderbook/orders count)))
        (is (= 1 (-> engine :sell orderbook/orders count)))
        (is (= ["E" 250 10.7] ((juxt :from-address :amount :price) e)))
        (is (.equals a order-a))
        (is (.equals b order-b))
        (is (.equals d order-d))))


    ;; ===========================================================
    ;; Scenario: Matching a large Sell order partially
    ;;   When we have previous test state (no crossing orders)
    ;;   When the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | E    | Sell | 350    | 10.4  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | E      | 100    | 10.4  |
    ;;   And market order book looks like:
    ;;   |---- buy(bids) ----------| ---- sell(asks) --------|
    ;;   | Addr   | Amount | Price | Price | Amount | Addr   |
    ;;   | B      | 200    | 10.3  | 10.3  | 250    | E      |
    ;;   |        |        |       | 10.7  | 100    | C      |
    ;;   |        |        |       | 10.8  | 200    | D      |
    (testing "match sell->buy partially"
        (let [engine (engine/accept-order @engine-state
                                          (assoc order-e
                                                 :price 10.4
                                                 :amount 350
                                                 :side order/SELL))
              [trade-a] (:trades engine)
              [b] (-> engine :buy orderbook/orders)
              [e c d] (-> engine :sell orderbook/orders)]
          ;; trade
          (is (= 1 (-> engine :trades count)))
          (is (.equals (trade/map->Trade {:buyer-address "A",
                                          :seller-address "E",
                                          :market "BTCETH",
                                          :amount 100,
                                          :price 10.4})
                       trade-a))
          ;; rest orders
          (is (= 1 (-> engine :buy orderbook/orders count)))
          (is (= 3 (-> engine :sell orderbook/orders count)))
          (is (= ["E" 250 10.4] ((juxt :from-address :amount :price) e)))
          (is (.equals b order-b))
          (is (.equals c order-c))
          (is (.equals d order-d))))))
