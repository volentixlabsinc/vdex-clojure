(ns matching-engine.orderbook-best-limit-test
  (:require
   [clojure.test :refer :all]
   [matching-engine.order     :as order]
   [matching-engine.orderbook :as orderbook]))

(deftest best-limit-test
  (testing "getting best limit price"
    (doseq [side [order/BUY order/SELL]]
      (let [order-a {:from-address "A"
                     :market "BTCETH"
                     :side side
                     :amount 100
                     :price :mp}
            order-b {:from-address "B"
                     :market "BTCETH"
                     :side side
                     :amount 100
                     :price 10.5}
            order-c {:from-address "C"
                     :market "BTCETH"
                     :side side
                     :amount 100
                     :price (if (= side order/BUY) 10.4 10.6)}
            order-d {:from-address "D"
                     :market "BTCETH"
                     :side side
                     :amount 100
                     :price (if (= side order/BUY) 10.6 10.4)}]
        ;; When there are no orders in the book, the best limit is not defined
        ;; Then the best limit for "<Side>" order book is "nil"
        (testing "empty orderbook"
          (let [book (orderbook/from-orders [] "BTCETH" side)]
            (is (-> book orderbook/best-limit-price nil?))))

        ;; If a market order enters the book, the best limit is still undefined
        ;;   When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price |
        ;;   | A    | 100    | MO    |
        ;;   Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price |
        ;;   | A    | 100    | MO    |
        ;;   And the best limit for "<Side>" order book is "nil"
        (testing "best price is nil (MO exist)"
          (let [book (orderbook/from-orders [order-a] "BTCETH" side)]
            (is (= order-a
                   (-> book orderbook/orders first)))
            (is (-> book orderbook/best-limit-price nil?))))

        ;; When the following orders are added to the "<Side>" book:
        ;; | Addr | Amount | Price |
        ;; | A    | 100    | MO    |
        ;; | B    | 100    | 10.5  |
        ;; Then the "<Side>" order book looks like:
        ;; | Addr | Amount | Price |
        ;; | A    | 100    | MO    |
        ;; | B    | 100    | 10.5  | <First Limit>
        ;; And the best limit for "<Side>" order book is "<First Limit> = 10.5"
        (testing "best price is equal first limit order price"
          (let [book (orderbook/from-orders [order-a order-b] "BTCETH" side)]
            (is (= order-b
                   (-> book orderbook/orders second)))
            (is (= (:price order-b)
                   (orderbook/best-limit-price book)))))

        ;; If a second, more conservative limit order enters the book, the best limit does not change
        ;;   When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price                |
        ;;   | A    | 100    | MO                   |
        ;;   | B    | 100    | 10.5                 |
        ;;   | C    | 100    | <B = 10.4 , S = 10.6>| <Convervative price>
        ;;   Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price        |
        ;;   | A    | 100    | MO           |
        ;;   | B    | 100    | 10.5         | <First Limit>
        ;;   | C    | 100    | 10.4 or 10.6 | <Convervative Limit>
        ;;   And the best limit for "<Side>" order book is "<First Limit> = 10.5"
        (testing "best price is equal first limit order price"
          (let [book (orderbook/from-orders [order-a order-b order-c] "BTCETH" side)]
            (is (= order-c
                   (-> book orderbook/orders last)))
            (is (= 3
                   (-> book orderbook/orders count)))
            (is (= (:price order-b)
                   (orderbook/best-limit-price book)))))

        ;; If a third, more aggresive limit order enters the book, it becomes best limit price
        ;;   When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price                |
        ;;   | A    | 100    | MO                   |
        ;;   | B    | 100    | 10.5                 |
        ;;   | C    | 100    | <B = 10.4 , S = 10.6>| <Convervative price>
        ;;   | D    | 100    | <B = 10.6 , S = 10.4>| <Aggresive price>
        ;;   Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price        |
        ;;   | A    | 100    | MO           |
        ;;   | D    | 100    | 10.6 or 10.4 | <Aggresive Limit>
        ;;   | B    | 100    | 10.5         | <First Limit>
        ;;   | C    | 100    | 10.4 or 10.6 | <Convervative Limit>
        ;;   And the best limit for "<Side>" order book is "<Aggresive Limit>"
        (testing "new best limit order price becomes best limit price"
          (let [book (orderbook/from-orders [order-a order-b order-c order-d] "BTCETH" side)]
            (is (= order-d
                   (-> book orderbook/orders second)))
            (is (= 4
                   (-> book orderbook/orders count)))
            (is (= (:price order-d)
                   (orderbook/best-limit-price book)))))

        ;; If the top MO is removed (i.e. because it crossed) the best limit should not change
        ;;   When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price                |
        ;;   | A    | 100    | MO                   |
        ;;   | B    | 100    | 10.5                 |
        ;;   | C    | 100    | <B = 10.4 , S = 10.6>| <Convervative price>
        ;;   | D    | 100    | <B = 10.6 , S = 10.4>| <Aggresive price>
        ;;   When the top order goes away (drained) from the "<Side>" book
        ;;   Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price          |
        ;;   | D    | 100    | <10.4 or 10.6> | <Aggresive Limit>
        ;;   | B    | 100    | 10.5           | <First Limit>
        ;;   | C    | 100    | <10.4 or 10.6> | <Convervative Limit>
        ;;   And the best limit for "<Side>" order book is "<Aggresive Limit>"
        (testing "best price is equal first limit order price"
          (let [book (-> [order-a order-b order-c order-d]
                         (orderbook/from-orders "BTCETH" side)
                         (orderbook/drain-top-order (:amount order-a)))]
            (is (= order-d
                   (-> book orderbook/orders first)))
            (is (= 3
                   (-> book orderbook/orders count)))
            (is (= (:price order-d)
                   (orderbook/best-limit-price book)))))

        ;; If the top aggressive order is removed (i.e. because it crossed) the best limit should become our first order
        ;;   When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price                |
        ;;   | A    | 100    | MO                   |
        ;;   | B    | 100    | 10.5                 | <First price>
        ;;   | C    | 100    | <B = 10.4 , S = 10.6>| <Convervative price>
        ;;   | D    | 100    | <B = 10.6 , S = 10.4>| <Aggresive price>
        ;;   When the top order A goes away (drained) from the "<Side>" book
        ;;   When the top order C goes away (drained) from the "<Side>" book
        ;;   Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price          |
        ;;   | B    | 100    | 10.5           | <First Limit>
        ;;   | C    | 100    | <10.4 or 10.6> | <Convervative Limit>
        ;;   And the best limit for "<Side>" order book is "<First Limit>"
        (testing "best price is changed after aggresive order was drained"
          (let [book (-> [order-a order-b order-c order-d]
                         (orderbook/from-orders "BTCETH" side)
                         (orderbook/drain-top-order (:amount order-a))
                         (orderbook/drain-top-order (:amount order-d)))]
            (is (= order-b
                   (-> book orderbook/orders first)))
            (is (= 2
                   (-> book orderbook/orders count)))
            (is (= (:price order-b)
                   (orderbook/best-limit-price book)))))


        ;; If the last two orders are removed (i.e. because they crossed) the best limit should become "None"
        ;;   When the following orders are added to the "<Side>" book:
        ;;   | Addr | Amount | Price                |
        ;;   | A    | 100    | MO                   |
        ;;   | B    | 100    | 10.5                 | <First price>
        ;;   | C    | 100    | <B = 10.4 , S = 10.6>| <Convervative price>
        ;;   | D    | 100    | <B = 10.6 , S = 10.4>| <Aggresive price>
        ;;   When the top order A goes away (drained) from the "<Side>" book
        ;;   When the top order C goes away (drained) from the "<Side>" book
        ;;   When the top order B goes away (drained) from the "<Side>" book
        ;;   When the top order D goes away (drained) from the "<Side>" book
        ;;   Then the "<Side>" order book looks like:
        ;;   | Addr | Amount | Price |
        ;;   And the best limit for "<Side>" order book is "nil"
        (testing "best price is nil after all orders was drained"
          (let [book (-> [order-a order-b order-c order-d]
                         (orderbook/from-orders "BTCETH" side)
                         (orderbook/drain-top-order (:amount order-a))
                         (orderbook/drain-top-order (:amount order-d))
                         (orderbook/drain-top-order (:amount order-b))
                         (orderbook/drain-top-order (:amount order-c)))]
            (is (-> book orderbook/orders empty?))
            (is (-> book orderbook/best-limit-price nil?))))))))
