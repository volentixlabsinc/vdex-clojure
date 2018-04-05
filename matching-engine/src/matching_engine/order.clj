(ns matching-engine.order)

(def BUY 1)
(def SELL 2)

(defn- market-price? [price]
  (= price :mp))

(defprotocol IOrder
  (limit-price [order])
  (market-order? [order])
  (cross-at-price? [order price]))

(defrecord Order [from-address market side amount price]
  IOrder
  (limit-price [order]
    (when-not (= price :mp)
      price))
  (market-order? [this]
    (market-price? price))
  (cross-at-price? [this compare-price]
    (when (and (-> price nil? not)
               (-> compare-price nil? not)
               (> amount 0))
      (if (or (market-price? price)
              (market-price? compare-price))
        true
        (if (and (> price 0)
                 (> compare-price 0))
          (if (= side BUY)
            (<= compare-price price)
            (>= compare-price price)))))))
