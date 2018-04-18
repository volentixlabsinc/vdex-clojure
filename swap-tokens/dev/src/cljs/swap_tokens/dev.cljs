(ns swap-tokens.dev
  (:require
    [figwheel.client :as fw]
    [swap-tokens.core]))

(enable-console-print!)

(fw/start {:on-jsload swap-tokens.core/init
           :websocket-url "ws://localhost:3449/figwheel-ws"})

;; display Hello World
(set! (.-innerText (.getElementById js/document "main")) "Hello World")
