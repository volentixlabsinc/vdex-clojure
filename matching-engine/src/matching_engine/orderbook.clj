(ns matching-engine.orderbook
  (:require
    [matching-engine.order :as order]))

(defn- compare-prices [side price-a price-b]
  (if (= side order/BUY)
    (compare price-b price-a)
    (compare price-a price-b)))

(defn- put-limit-order
  [limit-orders side {:keys [price] :as order}]
  (loop [[bucket & rest] limit-orders
         acc []]
    (if (empty? bucket)
      (conj acc [price [order]])
      (let [[level-limit orders] bucket
            price-case (compare-prices side price level-limit)]
        (case price-case
          ;; order price same as level price
          ;; add order into current bucket
          0
          (conj acc
                (update bucket 1 conj order))

          ;; order price better than level price
          ;; prepend order as new bucket
          -1
          (-> acc
              (conj [price [order]])
              (conj bucket)
              (concat rest)
              vec)
          ;; try next bucket
          (recur rest (conj acc bucket)))))))

(defn- drain-top-market-order
  "Decreace top market order amount (as an result of match between orders)"
  [market-orders amount]
  (let [[top-order & rest-orders] market-orders
        drained? (= amount (:amount top-order))
        new-top-order (when-not drained?
                        (update top-order :amount - amount))]
    (if drained?
      rest-orders
      (cons new-top-order rest-orders))))


(defn- drain-top-limit-order
  "Decreace top limit order amount (as an result of match between orders)"
  [limit-orders amount]
  (let [[bucket & rest-buckets] limit-orders
        [price-level orders] bucket
        [top-order & rest-orders] orders
        drained? (= amount (:amount top-order))
        new-top-order (when-not drained?
                        (update top-order :amount - amount))]
    (case [drained? (empty? rest-orders)]
      [true true]
      rest-buckets

      [true false]
      (cons [price-level rest-orders] rest-buckets)

      [false false]
      (cons [price-level
             (cons new-top-order
                   rest-orders)]
            rest-buckets)

      (cons [price-level [new-top-order]]
            rest-buckets))))


(defprotocol IOrderBook
  (add-order [orderbook order])
  (orders [orderbook])
  (top-order [orderbook])
  (best-limit-price [this])
  (drain-top-order [this amount]))

(defrecord OrderBook [market side limit-orders market-orders]
  IOrderBook
  (add-order [this {:keys [price] :as order}]
    (let [put-order (fn [o]
                      (if (= (:price order) :mp)
                        (update this
                                :market-orders
                                concat
                                (list order))
                        (update this :limit-orders put-limit-order side order)))]
      (put-order (order/map->Order order))))
  (orders [this]
    (concat market-orders
            (->> limit-orders
                 (map second)
                 flatten)))
  (top-order [this]
    (if-let [top (first market-orders)]
      top
      (-> limit-orders first second first)))
  (best-limit-price [this]
    (ffirst limit-orders))
  (drain-top-order [this amount]
    (if-not (empty? market-orders)
      (update this :market-orders drain-top-market-order amount)
      (update this :limit-orders drain-top-limit-order amount))))

(defn init
  "Initialize new OrderBook"
  [opts-map]
  (map->OrderBook (merge opts-map
                         {:limit-orders []
                          :market-orders []})))

(defn from-orders
  [orders market-name side]
  (->> orders
       (filter #(-> % :side (= side)))
       (reduce add-order
               (init {:market market-name
                      :side side}))))
