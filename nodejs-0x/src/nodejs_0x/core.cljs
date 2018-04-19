(ns nodejs-0x.core
  (:require 
    [cljs.nodejs :as nodejs]))


(nodejs/enable-util-print!)
(def Web3 (nodejs/require "web3"))
(def ZeroEx (nodejs/require "0x.js"))
(def Utils (nodejs/require "@0xproject/utils"))
(set! js/Web3 Web3)
(set! js/ZeroEx ZeroEx)
(set! js/Utils Utils)


(def decimals 18)


(defn- to-base-unit-amount
  [amount]
  (ZeroEx.ZeroEx.toBaseUnitAmount amount decimals))


(defn- await-transaction-mined
  [zero-ex allow-tx-hash]
  (.awaitTransactionMinedAsync zero-ex allow-tx-hash))


(defn- order-filled
  [tx-hash zero-ex]
  (println (str "Transaction hash: " tx-hash))
  (-> (.awaitTransactionMinedAsync zero-ex tx-hash) (.then #(println (str "FillOrder transaction receipt: " %)))))


(defn- order-validated
  [error zero-ex taker-address signed-order]
  (if-not error
      (let [_ (println "Validation successfull, order is fillable.")
            should-throw-on-insufficient-balance-or-allowance true
            fill-taker-token-amount (to-base-unit-amount (new Utils.BigNumber 0.1))]
        (-> (.fillOrderAsync (.-exchange zero-ex) 
                             signed-order 
                             fill-taker-token-amount 
                             should-throw-on-insufficient-balance-or-allowance
                             taker-address) 
            (.then #(order-filled % zero-ex))))
      (println "Validation failed, order is not fillable.")))


(defn- signed-order-hash
  [ec-signature order-vector zero-ex taker-address]
  (let [signed-order (apply js-obj (concat order-vector ["ecSignature" ec-signature]))]
    (try (-> (.validateOrderFillableOrThrowAsync (.-exchange zero-ex) signed-order) 
             (.then #(order-validated % zero-ex taker-address signed-order))) 
         (catch js/Error e (println e)))))


(defn- then-accounts
  [accounts zero-ex zrx-address weth-address exchange-address]
  (println "Accounts: " accounts "\n")
  (let [maker-address (first accounts)
        taker-address (second accounts)]
    (println "Setting allowance...")
    (-> (.setUnlimitedProxyAllowanceAsync (.-token zero-ex) zrx-address maker-address) 
        (.then #(await-transaction-mined zero-ex %)))
    (-> (.setUnlimitedProxyAllowanceAsync (.-token zero-ex) weth-address taker-address) 
        (.then #(await-transaction-mined zero-ex %)))
    (let [eth-amount (new Utils.BigNumber 1)
          eth-to-convert (to-base-unit-amount eth-amount)
          order-vector ["maker" maker-address
                        "taker" taker-address
                        "feeRecipient" (.-NULL_ADDRESS ZeroEx.ZeroEx)
                        "makerTokenAddress" zrx-address
                        "takerTokenAddress" weth-address
                        "exchangeContractAddress" exchange-address
                        "salt" (ZeroEx.ZeroEx.generatePseudoRandomSalt)
                        "makerFee" (new Utils.BigNumber 0)
                        "takerFee" (new Utils.BigNumber 0)
                        "makerTokenAmount" (to-base-unit-amount (new Utils.BigNumber 0.2))
                        "takerTokenAmount" (to-base-unit-amount (new Utils.BigNumber 0.3))
                        "expirationUnixTimestampSec" (new Utils.BigNumber (+ (. (js/Date.) (getTime)) 3600000))]
          order (apply js-obj order-vector)
          order-hash (try (ZeroEx.ZeroEx.getOrderHashHex order) (catch js/Error e (println e)))
          should-add-personal-message-prefix false]
      (-> (.signOrderHashAsync zero-ex order-hash maker-address should-add-personal-message-prefix) 
          (.then #(signed-order-hash % order-vector zero-ex taker-address))))))


(defn -main [& args]
  (let [provider (new Web3.providers.HttpProvider "http://localhost:8545")
        zero-ex (new ZeroEx.ZeroEx provider (js-obj "networkId" 50))
        weth-address (.getContractAddressIfExists (.-etherToken zero-ex))
        zrx-address (.getZRXTokenAddress (.-exchange zero-ex))
        exchange-address (.getContractAddress (.-exchange zero-ex))
        ]
    (println (str "WETH address: " weth-address))
    (println (str "ZRX address: " zrx-address))
    (println (str "Exchange address: " exchange-address "\n"))
    (-> (.getAvailableAddressesAsync zero-ex) (.then #(then-accounts % zero-ex zrx-address weth-address exchange-address)))))


(set! *main-cli-fn* -main)
