(ns matching-engine.orderbook-market-test
  (:require
   [clojure.test :refer :all]
   [matching-engine.order     :as order]
   [matching-engine.orderbook :as orderbook]))

(deftest buy-orderbook-test
  (testing "buy orders (bids) are sorted desc by price"
    (doseq [side [order/BUY order/SELL]]
      (let [order-a {:from-address "A"
                    :market "BTCETH"
                    :side side
                    :amount 100
                    :price 10.5}
            order-b {:from-address "B"
                    :market "BTCETH"
                    :side side
                    :amount 100
                     :price :mp}
            order-c {:from-address "C"
                     :market "BTCETH"
                     :side side
                     :amount 100
                     :price 10.5}
            order-d {:from-address "D"
                     :market "BTCETH"
                     :side side
                     :amount 100
                     :price :mp}]

        ;; When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price |
        ;;   | A    | 100    | 10.5  |
        ;; Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price |
        ;;   | A    | 100    | 10.5  |
        (testing "add one order"
          (let [book (orderbook/from-orders [order-a] "BTCETH" side)]
            (is (= order-a
                   (-> book orderbook/orders first)))))

        ;; When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price |
        ;;   | A    | 100    | 10.5  |
        ;;   | B    | 100    | MO    |
        ;; Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price |
        ;;   | B    | 100    | MO    |
        ;;   | A    | 100    | 10.5  |
        (testing "add two orders"
          (let [book (orderbook/from-orders [order-a order-b] "BTCETH" side)]
            (is (= order-b
                   (-> book orderbook/orders first)))))

        ;; When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price |
        ;;   | A    | 100    | 10.5  |
        ;;   | B    | 100    | MO    |
        ;;   | C    | 100    | 10.5  |
        ;; Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price |
        ;;   | B    | 100    | MO    |
        ;;   | A    | 100    | 10.5  |
        ;;   | C    | 100    | 10.5  |
        (let [book (orderbook/from-orders [order-a order-b order-c] "BTCETH" side)]
          (is (= order-c
                 (-> book orderbook/orders last))))

        ;; When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price |
        ;;   | A    | 100    | 10.5  |
        ;;   | B    | 100    | MO    |
        ;;   | C    | 100    | 10.5  |
        ;;   | D    | 100    | MO    |
        ;; Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price |
        ;;   | B    | 100    | MO    |
        ;;   | D    | 100    | MO    |
        ;;   | A    | 100    | 10.5  |
        ;;   | C    | 100    | 10.5  |
        (let [book (orderbook/from-orders [order-a order-b order-c order-d] "BTCETH" side)]
          (is (= order-d
                 (-> book orderbook/orders second))))))))
