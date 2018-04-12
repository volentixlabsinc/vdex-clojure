(ns matching-engine.engine
  (:require
    [rop.core         :as rop]
    [matching-engine.order     :as order]
    [matching-engine.orderbook :as orderbook]
    [matching-engine.trade     :as trade]))

(defn- make-trade
  "Create Trade as an result of matching order and sell order."
  [{:keys [side market] :as order} top-order counter-orderbook reference-price]
  (let [oposite-best-price (orderbook/best-limit-price counter-orderbook)
        [buy sell] (cond-> [order top-order]
                     (= side order/SELL) reverse)
        best-price (or (order/limit-price top-order)
                       (when (order/cross-at-price? order
                                                    oposite-best-price)
                         oposite-best-price)
                       (order/limit-price order)
                       reference-price)]
    (when (order/cross-at-price? order (:price top-order))
      (trade/map->Trade {:buyer-address (:from-address buy)
                         :seller-address (:from-address sell)
                         :market market
                         :amount (min (:amount buy) (:amount sell))
                         :price best-price}))))

(defn =validate-amount=
  "Check if amount is > 0. Returns :invalid-amounty if not."
  [{:keys [order] :as ctx}]
  (if (> (:amount order) 0)
    (rop/succeed ctx)
    (rop/fail :invalid-amount)))

(defn =validate-price=
  "Check if price is > 0 or price is market price. Returns :invalid-price if not"
  [{:keys [order] :as ctx}]
  (if (or (= (:price order) :mp)
          (> (:price order) 0))
    (rop/succeed ctx)
    (rop/fail :invalid-price)))

(defn =make-trade=
  "Update internal state if there is match between buy and sell order.
   Steps:
   - add Trade into trades collection
   - decreace found matching order amount by trade amount
   - decreace source order amount by trade amount
   If matching order is matching fully then it will be removed from counter orderbook"
  [{:keys [order engine counter-orderbook reference-price] :as ctx}]
  (if-let [top-order (orderbook/top-order counter-orderbook)]
    (if-let [trade (make-trade order
                               top-order
                               counter-orderbook
                               reference-price)]
        (recur (-> ctx
                   (update-in [:engine :trades] conj trade)
                   (update :counter-orderbook orderbook/drain-top-order (:amount trade))
                   (update-in [:order :amount] - (:amount trade))
                   (assoc-in [:engine :reference-price] (:price trade))))
      (rop/succeed ctx))
    (rop/succeed ctx)))

(defn =save-unfilled-order=
  "Add source order into orders collection if we didn't find orders to cover it fully."
  [{:keys [order] :as ctx}]
  (if (> (:amount order) 0)
    (rop/succeed (update ctx :orderbook orderbook/add-order order))
    (rop/succeed ctx)))

(defn =update-engine-books=
  [{:keys [engine order orderbook counter-orderbook] :as ctx}]
  (let [[buy sell] (cond-> [orderbook counter-orderbook]
                     (= (:side order) order/SELL) reverse)]
    (rop/succeed (-> engine
                     (assoc :buy buy)
                     (assoc :sell sell)))))

(defprotocol IMatchingEngine
  (orderbooks [engine side])
  (accept-order [engine order])
  (accept-orders [engine orders]))

(defrecord MatchingEngine [buy sell accounts trades reference-price]
  IMatchingEngine
  (orderbooks [engine side]
    (if (= side order/BUY)
      [buy sell]
      [sell buy]))
  (accept-order [engine {:keys [side] :as order}]
    (let [[book counter-book] (orderbooks engine side)]
      (rop/>>= {:engine engine
                :side side
                :reference-price reference-price
                :order (order/map->Order order)
                :orderbook book
                :counter-orderbook counter-book}
               =validate-amount=
               =validate-price=
               =make-trade=
               =save-unfilled-order=
               =update-engine-books=)))
  (accept-orders [engine orders]
    (loop [[order & rest] orders
           engine' engine]
      (if-not order
        engine'
        (recur rest
               (accept-order engine' order))))))

(defn init
  "Initialize MatchingEngine with empty buy and sell orderbook"
  [market-name reference-price]
  (map->MatchingEngine {:buy (orderbook/init {:market market-name
                                              :side order/BUY})
                        :sell (orderbook/init {:market market-name
                                               :side order/SELL})
                        :accounts []
                        :trades []
                        :reference-price reference-price}))
