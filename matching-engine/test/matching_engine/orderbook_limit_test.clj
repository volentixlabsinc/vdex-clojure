(ns matching-engine.orderbook-limit-test
  (:require
   [clojure.test :refer :all]
   [matching-engine.order     :as order]
   [matching-engine.orderbook :as orderbook]))

(deftest buy-orderbook-test
  (testing "buy orders (bids) are sorted desc by price"
    (let [order-a {:from-address "A"
                   :market "BTCETH"
                   :side order/BUY
                   :amount 100
                   :price 10.5}
          order-b {:from-address "B"
                   :market "BTCETH"
                   :side order/BUY
                   :amount 100
                   :price 10.4}]
      ;; Scenario: Add a single limit order to the BUY order book
      ;;   When the following orders are added to the "Buy" book:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.5  |
      ;;   Then the "Buy" order book looks like:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.5  |
      (testing "add one order"
        (let [book (orderbook/from-orders [order-a] "BTCETH" order/BUY)]
          (is (= order-a
                 (-> book orderbook/orders first)))))

      ;; Scenario: Add two limit orders to the BUY order book, with better order first (higher price first)
      ;;   When the following orders are added to the "Buy" book:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.5  |
      ;;   | B    | 100    | 10.4  |
      ;;   Then the "Buy" order book looks like:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.5  |
      ;;   | B    | 100    | 10.4  |
      (testing "add two orders (better first)"
        (let [book (orderbook/from-orders [order-a order-b] "BTCETH" order/BUY)]
          (is (= order-a
                 (-> book orderbook/orders first)))
          (is (= order-b
                 (-> book orderbook/orders second)))))

      ;; Scenario: Add two limit orders to the BUY order book, with better order second (higher price second)
      ;;   When the following orders are added to the "Buy" book:
      ;;   | Addr | Amount | Price |
      ;;   | B    | 100    | 10.4  |
      ;;   | A    | 100    | 10.5  |
      ;;   Then the "Buy" order book looks like:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.5  |
      ;;   | B    | 100    | 10.4  |
      (testing "add two orders (worst first)"
        (let [book (orderbook/from-orders [order-b order-a] "BTCETH" order/BUY)]
          (is (= order-a
                 (-> book orderbook/orders first)))
          (is (= order-b
                 (-> book orderbook/orders second))))))))

(deftest sell-orderbook-test
  (testing "sell orders (asks) are sorted asc by price"
    (let [order-a {:from-address "A"
                   :market "BTCETH"
                   :side order/SELL
                   :amount 100
                   :price 10.6}
          order-b {:from-address "B"
                   :market "BTCETH"
                   :side order/SELL
                   :amount 100
                   :price 10.7}]

      ;; Scenario: Add a single limit order to the SELL order book
      ;;   When the following orders are added to the "Sell" book:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.6  |
      ;;   Then the "Buy" order book looks like:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.6  |
      (testing "add one order"
        (let [book (orderbook/from-orders [order-a] "BTCETH" order/SELL)]
          (is (= order-a
                (-> book orderbook/orders first)))))

      ;; Scenario: Add two limit orders to the SELL order book, with better order first (lower price first)
      ;;   When the following orders are added to the "Sell" book:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.6  |
      ;;   | B    | 100    | 10.7  |
      ;;   Then the "Buy" order book looks like:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.6  |
      ;;   | B    | 100    | 10.7  |
      (testing "add two orders (better first)"
        (let [book (orderbook/from-orders [order-a order-b] "BTCETH" order/SELL)]
          (is (= order-a
                 (-> book orderbook/orders first)))
          (is (= order-b
                 (-> book orderbook/orders second)))))

      ;; Scenario: Add two limit orders to the SELL order book, with better order second (lower price second)
      ;;   When the following orders are added to the "Sell" book:
      ;;   | Addr | Amount | Price |
      ;;   | B    | 100    | 10.7  |
      ;;   | A    | 100    | 10.6  |
      ;;   Then the "Buy" order book looks like:
      ;;   | Addr | Amount | Price |
      ;;   | A    | 100    | 10.6  |
      ;;   | B    | 100    | 10.7  |
      (testing "add two orders (worst first)"
        (let [book (orderbook/from-orders [order-b order-a] "BTCETH" order/SELL)]
          (is (= order-a
                 (-> book orderbook/orders first)))
          (is (= order-b
                 (-> book orderbook/orders second))))))))
