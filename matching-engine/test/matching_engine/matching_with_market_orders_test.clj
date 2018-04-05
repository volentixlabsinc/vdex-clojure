(ns matching-engine.matching-with-market-orders-test
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
   :amount 120
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

(deftest matching-LO->MO-orders-test
  (testing "matching with market orders (LO->MO)"

    ;; Scenario: Matching incoming Buy limit order against a single outstanding Sell market order
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Sell | 100    | MO    |
    ;;   | B    | Buy  | 120    | 10.5  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | B     | A      | 100    | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | B    | 20     | 10.5  |       |        |      |
    (testing "case 1"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price :mp :side order/SELL))
                       (engine/accept-order (assoc order-b :price 10.5 :side order/BUY)))
            [trade-a] (:trades engine)
            [b] (-> engine :buy orderbook/orders)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "B",
                                        :seller-address "A",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.5})
                     trade-a))
        ;; rest orders
        (is (= ["B" 20 10.5] ((juxt :from-address :amount :price) b)))))

    ;; Scenario: Matching incoming Sell limit order against a single outstanding Buy market order
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | MO    |
    ;;   | B    | Sell | 120    | 10.5  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | B      | 100    | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   |      |        |       | B     | 20     |      |
    (testing "case 2"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price :mp :side order/BUY))
                       (engine/accept-order (assoc order-b :price 10.5 :side order/SELL)))
            [trade-a] (:trades engine)
            [b] (-> engine :sell orderbook/orders)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "B",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.5})
                     trade-a))
        ;; rest orders
        (is (= ["B" 20 10.5] ((juxt :from-address :amount :price) b)))))

    ;; Scenario: Matching incoming Buy limit order against Sell market order while another NON-CROSSING Sell limit order is outstanding
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Sell | 100    | MO    |
    ;;   | B    | Sell | 100    | 10.6  |
    ;;   | C    | Buy  | 120    | 10.5  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | C     | A      | 100    | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | C    | 20     | 10.5  | 10.6  | 100    | B    |
    (testing "case 3"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/SELL))
                       (engine/accept-order (assoc order-b :price 10.6 :amount 100 :side order/SELL))
                       (engine/accept-order (assoc order-c :price 10.5 :amount 120 :side order/BUY)))
            [trade-a] (:trades engine)
            [c] (-> engine :buy orderbook/orders)
            [b] (-> engine :sell orderbook/orders)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "C",
                                        :seller-address "A",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.5})
                     trade-a))
        ;; rest orders
        (is (= ["C" 20 10.5] ((juxt :from-address :amount :price) c)))
        (is (= ["B" 100 10.6] ((juxt :from-address :amount :price) b)))))

;; Scenario: Matching incoming Buy limit order against Sell market order while another NON-CROSSING Sell limit order is outstanding
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Sell | 100    | MO    |
    ;;   | B    | Sell | 100    | 10.4  |
    ;;   | C    | Buy  | 120    | 10.5  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | C     | A      | 100    | 10.4  |
    ;;   | C     | B      | 20     | 10.4  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   |      |        |       | 10.4  | 80     | B    |
    (testing "case 4"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/SELL))
                       (engine/accept-order (assoc order-b :price 10.4 :amount 100 :side order/SELL))
                       (engine/accept-order (assoc order-c :price 10.5 :amount 120 :side order/BUY)))
            [trade-a trade-b] (:trades engine)
            [b] (-> engine :sell orderbook/orders)]
        ;; trades
        (is (= 2 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "C",
                                        :seller-address "A",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.4})
                     trade-a))
        (is (.equals (trade/map->Trade {:buyer-address "C",
                                        :seller-address "B",
                                        :market "BTCETH",
                                        :amount 20,
                                        :price 10.4})
                     trade-b))
        ;; rest orders
        (is (= ["B" 80 10.4] ((juxt :from-address :amount :price) b)))))

    ;; Scenario: Matching incoming Sell limit order against Sell market order while another NON-CROSSING Buy limit order is outstanding
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | MO    |
    ;;   | B    | Buy  | 100    | 10.4  |
    ;;   | C    | Sell | 120    | 10.5  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | C      | 100    | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | B    | 100    | 10.4  | 10.5  | 20     | C    |
    (testing "case 5"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-b :price 10.4 :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-c :price 10.5 :amount 120 :side order/SELL)))
            [trade-a] (:trades engine)
            [b] (-> engine :buy orderbook/orders)
            [c] (-> engine :sell orderbook/orders)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "C",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.5})
                     trade-a))
        ;; rest orders
        (is (= ["C" 20 10.5] ((juxt :from-address :amount :price) c)))
        (is (= ["B" 100 10.4] ((juxt :from-address :amount :price) b)))))

    ;; Scenario: Matching incoming Sell limit order against Sell market order while another NON-CROSSING Buy limit order is outstanding
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | MO    |
    ;;   | B    | Buy  | 100    | 10.6  |
    ;;   | C    | Sell | 120    | 10.5  |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | C      | 100    | 10.6  |
    ;;   | B     | C      | 20     | 10.6  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | B    | 80     | 10.6  |       |        |      |
    (testing "case 6"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-b :price 10.6 :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-c :price 10.5 :amount 120 :side order/SELL)))
            [trade-a trade-b] (:trades engine)
            [b] (-> engine :buy orderbook/orders)]
        ;; trades
        (is (= 2 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "C",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.6})
                     trade-a))
        (is (.equals (trade/map->Trade {:buyer-address "B",
                                        :seller-address "C",
                                        :market "BTCETH",
                                        :amount 20,
                                        :price 10.6})
                     trade-b))
        ;; rest orders
        (is (= ["B" 80 10.6] ((juxt :from-address :amount :price) b)))))

    ;; Scenario: Matching incoming Buy market order against Sell market order when another - limit - Sell order present
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Sell | 100    | MO    |
    ;;   | B    | Sell | 100    | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   |      |        |       | MO    | 100    | A    |
    ;;   |      |        |       | 10.5  | 100    | B    |
    ;;   When the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | C    | Buy  | 100    | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | C     | A      | 100    | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   |      |        |       | 10.5  | 100    | B    |
    (testing "case 7"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/SELL))
                       (engine/accept-order (assoc order-b :price 10.5 :amount 100 :side order/SELL))
                       (engine/accept-order (assoc order-c :price :mp :amount 100 :side order/BUY)))
            [trade-a] (:trades engine)
            [b] (-> engine :sell orderbook/orders)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "C",
                                        :seller-address "A",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.5})
                     trade-a))
        ;; rest orders
        (is (= ["B" 100 10.5] ((juxt :from-address :amount :price) b)))))

    ;; Scenario: Matching incoming Sell market order against Buy market order when another - limit - Buy order present
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy | 100     | MO    |
    ;;   | B    | Buy | 100     | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | A    | 100    | MO    |       |        |      |
    ;;   | B    | 100    | 10.5  |       |        |      |
    ;;   When the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | C    | Sell | 100    | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | C      | 100    | 10.5  |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    ;;   | B    | 100    | 10.5  |       |        |      |
    (testing "case 8"
      (let [engine (-> (engine/init "BTCETH" nil)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-b :price 10.5 :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-c :price :mp :amount 100 :side order/SELL)))
            [trade-a] (:trades engine)
            [b] (-> engine :buy orderbook/orders)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "C",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10.5})
                     trade-a))
        ;; rest orders
        (is (= ["B" 100 10.5] ((juxt :from-address :amount :price) b)))))

    ;; Scenario: Matching incoming Buy market order against Sell market order when no best limit price is available
    ;;   Given the reference price is set to "10"
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Sell | 100    | MO    |
    ;;   | B    | Buy  | 100    | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | B     | A      | 100    | 10    |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    (testing "case 9"
      (let [engine (-> (engine/init "BTCETH" 10)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/SELL))
                       (engine/accept-order (assoc order-b :price :mp :amount 100 :side order/BUY)))
            [trade-ab] (:trades engine)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "B",
                                        :seller-address "A",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10})
                     trade-ab))
        ;; rest orders
        (is (-> engine :buy orderbook/orders empty?))
        (is (-> engine :sell orderbook/orders empty?))))

    ;; Scenario: Matching incoming Sell market order against Buy market order when no best limit price is available
    ;;   Given the reference price is set to "10"
    ;;   Given the following orders are submitted in this order:
    ;;   | Addr | Side | Amount | Price |
    ;;   | A    | Buy  | 100    | MO    |
    ;;   | B    | Sell | 100    | MO    |
    ;;   Then the following trades are generated:
    ;;   | Buyer | Seller | Amount | Price |
    ;;   | A     | B      | 100    | 10    |
    ;;   And engine books look like:
    ;;   |---- buy(bids) --------| ---- sell(asks) ------|
    ;;   | Addr | Amount | Price | Price | Amount | Addr |
    (testing "case 10"
      (let [engine (-> (engine/init "BTCETH" 10)
                       (engine/accept-order (assoc order-a :price :mp :amount 100 :side order/BUY))
                       (engine/accept-order (assoc order-b :price :mp :amount 100 :side order/SELL)))
            [trade-ab] (:trades engine)]
        ;; trades
        (is (= 1 (-> engine :trades count)))
        (is (.equals (trade/map->Trade {:buyer-address "A",
                                        :seller-address "B",
                                        :market "BTCETH",
                                        :amount 100,
                                        :price 10})
                     trade-ab))
        ;; rest orders
        (is (-> engine :buy orderbook/orders empty?))
        (is (-> engine :sell orderbook/orders empty?))))))
