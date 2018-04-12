(ns matching-engine.matching-from-market-orders-test
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

(def order-c
  {:from-address "C"
   :market "BTCETH"
   :side order/SELL
   :amount 300
   :price nil})

(def order-d
  {:from-address "D"
   :market "BTCETH"
   :side -1
   :amount 650
   :price nil})

(deftest matching-MO->LO-orders-test
  (testing "matching against market orders (MO->LO)"

    ;; Scenario: Matching a large Sell market order against multiple limit orders
    ;; The order is large enough to fill the entire opposite book
    ;; The remainder of the market order is expected to rest in its book
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | 10.7  |
    ;;   | B    | Buy  | 200    | 10.6  |
    ;;   | C    | Buy  | 300    | 10.5  |
    ;;   | D    | Sell | 650    | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | D      | 100    | 10.7  |
    ;;   | B     | D      | 200    | 10.6  |
    ;;   | C     | D      | 300    | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | B    |        |       | MO    | 50     | D    |
    (testing "case 1"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price 10.7 :side order/BUY))
                       (engine/accept-order (assoc order-b :price 10.6 :side order/BUY))
                       (engine/accept-order (assoc order-c :price 10.5 :side order/BUY))
                       (engine/accept-order (assoc order-d :price :mp :side order/SELL)))
            [trade-a trade-b trade-c] (:trades engine)
            [d] (-> engine :sell orderbook/orders)]
        ;; no trades
        (is (= 3 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "D",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.7})
                     trade-a))
        (is (.equals (trade/map->Trade {:buyer-address "B",
                                        :seller-address "D",
                                        :market "BTCETH",
                                        :amount 200,
                                        :price 10.6})
                     trade-b))
        (is (.equals (trade/map->Trade {:buyer-address "C",
                                        :seller-address "D",
                                        :market "BTCETH",
                                        :amount 300,
                                        :price 10.5})
                     trade-c))
        ;; rest orders
        (is (= ["D" 50 :mp] ((juxt :from-address :amount :price) d)))))

    ;; Scenario: Matching a large Buy market order against multiple limit orders
    ;; The order is large enough to fill the entire opposite book
    ;; The remainder of the market order is expected to rest in its book
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Sell  | 100   | 10.5  |
    ;;   | B    | Sell  | 200   | 10.6  |
    ;;   | C    | Sell  | 300   | 10.7  |
    ;;   | D    | Buy   | 650   | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | D     | A      | 100    | 10.5  |
    ;;   | D     | B      | 200    | 10.6  |
    ;;   | D     | C      | 300    | 10.7  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | A    | 50     | MO    |       |        |      |
    (testing "case 2"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price 10.5 :side order/SELL))
                       (engine/accept-order (assoc order-b :price 10.6 :side order/SELL))
                       (engine/accept-order (assoc order-c :price 10.7 :side order/SELL))
                       (engine/accept-order (assoc order-d :price :mp :side order/BUY)))
            [trade-a trade-b trade-c] (:trades engine)
            [d] (-> engine :buy orderbook/orders)]
        ;; no trades
        (is (= 3 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "D",
                                        :seller-address "A",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.5})
                     trade-a))
        (is (.equals (trade/map->Trade {:buyer-address "D",
                                        :seller-address "B",
                                        :market "BTCETH",
                                        :amount 200,
                                        :price 10.6})
                     trade-b))
        (is (.equals (trade/map->Trade {:buyer-address "D",
                                        :seller-address "C",
                                        :market "BTCETH",
                                        :amount 300,
                                        :price 10.7})
                     trade-c))
        ;; rest orders
        (is (= ["D" 50 :mp] ((juxt :from-address :amount :price) d)))))

    ;; Scenario: Matching a small Sell market order against multiple limit orders
    ;; The order is large enough to fill the entire opposite book
    ;; The remainder of the market order is expected to rest in its book
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | 10.7  |
    ;;   | B    | Buy  | 200    | 10.6  |
    ;;   | C    | Buy  | 300    | 10.5  |
    ;;   | D    | Sell | 150    | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | D      | 100    | 10.7  |
    ;;   | B     | D      | 50     | 10.6  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | B    | 150    | 10.6  |       |        |      |
    ;;   | C    | 300    | 10.5  |       |        |      |
    (testing "case 3"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price 10.7 :side order/BUY))
                       (engine/accept-order (assoc order-b :price 10.6 :side order/BUY))
                       (engine/accept-order (assoc order-c :price 10.5 :side order/BUY))
                       (engine/accept-order (assoc order-d :price :mp :side order/SELL :amount 150)))
            [trade-a trade-b] (:trades engine)
            [b c] (-> engine :buy orderbook/orders)]
        ;; no trades
        (is (= 2 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "D",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.7})
                     trade-a))
        (is (.equals (trade/map->Trade {:buyer-address "B",
                                        :seller-address "D",
                                        :market "BTCETH",
                                        :amount 50,
                                        :price 10.6})
                     trade-b))
        ;; rest orders
        (is (= ["B" 150 10.6] ((juxt :from-address :amount :price) b)))
        (is (= ["C" 300 10.5] ((juxt :from-address :amount :price) c)))))

    ;; Scenario: Matching a small Buy market order against multiple limit orders
    ;; The order is large enough to fill the entire opposite book
    ;; The remainder of the market order is expected to rest in its book
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Sell  | 100   | 10.5  |
    ;;   | B    | Sell  | 200   | 10.6  |
    ;;   | C    | Sell  | 300   | 10.7  |
    ;;   | D    | Buy   | 150   | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | D     | A      | 100    | 10.5  |
    ;;   | D     | B      | 50     | 10.6  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   |      |        |       | 10.6  | 150    | B    |
    ;;   |      |        |       | 10.7  | 300    | C    |
    (testing "case 4"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price 10.5 :side order/SELL))
                       (engine/accept-order (assoc order-b :price 10.6 :side order/SELL))
                       (engine/accept-order (assoc order-c :price 10.7 :side order/SELL))
                       (engine/accept-order (assoc order-d :price :mp :side order/BUY :amount 150)))
            [trade-a trade-b] (:trades engine)
            [b c] (-> engine :sell orderbook/orders)]
        ;; no trades
        (is (= 2 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "D",
                                        :seller-address "A",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.5})
                     trade-a))
        (is (.equals (trade/map->Trade {:buyer-address "D",
                                        :seller-address "B",
                                        :market "BTCETH",
                                        :amount 50,
                                        :price 10.6})
                     trade-b))
        ;; rest orders
        (is (= ["B" 150 10.6] ((juxt :from-address :amount :price) b)))
        (is (= ["C" 300 10.7] ((juxt :from-address :amount :price) c)))))))
