(ns matching-engine.flow
  (:require
    [integrant.core :as ig]
    [clojurewerkz.meltdown.reactor   :as mr]
    [clojurewerkz.meltdown.selectors :as ms :refer [$ R]])
  (:import
    [java.util.concurrent   CountDownLatch TimeUnit]
    [reactor.event.dispatch WorkQueueDispatcher]
    [clojurewerkz.meltdown  DefaultingCachingRegistry]))

(defmacro with-latch
  [countdown-from & body]
  `(let [latch# (CountDownLatch. ~countdown-from)
         ;; intentionally unhygienic, expected by @body
         ~'latch latch#]
     ~@body
     #_(.await latch# 2 TimeUnit/SECONDS)))

;; == Processing flow
;; get order -> validate signature | -> fulfill order -> create transaction -> save to file
;;           -> check balane       |

#_(def dispatcher-types
  {:event-loop  "eventLoop"
   :work-queue  "workQueue"
   :thread-pool "threadPoolExecutor"
   :ring-buffer "ringBuffer"})
(comment
  (with-latch 1
    (let [r        (mr/create)
          data     {:order-id "123456"}
          key-vs   "validate-signature"
          key-cb   "check-balance"
          key-fo   "fill-order"
          key-ct   "create-order"
          key-sf   "save-as-file"
          print-fn (fn [event] (println event))]
      (mr/on r ($ key-vs) print-fn)
      (mr/on r ($ key-cb) print-fn)
      (mr/on r ($ key-fo) print-fn)
      (mr/on r ($ key-ct) print-fn)
      (mr/on r ($ key-sf) print-fn)

      (mr/notify r key-vs data)
      (mr/notify r key-cb data)
      (mr/notify r key-fo data)
      (mr/notify r key-ct data)
      (mr/notify r key-sf data)

      (.await latch 1 TimeUnit/SECONDS)))

)

;; ===== OUTPUT
;; {:data {:order-id 123456}, :reply-to nil, :headers {},
;;  :key validate-signature, :id #uuid "8874db81-2d30-11e8-b33f-402ebcfefb8d"}

;; {:data {:order-id 123456}, :reply-to nil, :headers {},
;;  :key check-balance, :id #uuid "8874db83-2d30-11e8-b33f-402ebcfefb8d"}

;; {:data {:order-id 123456}, :reply-to nil, :headers {},
;;  :key fill-order, :id #uuid "88750291-2d30-11e8-b33f-402ebcfefb8d"}

;; {:data {:order-id 123456}, :reply-to nil, :headers {},
;;  :key create-order, :id #uuid "88750293-2d30-11e8-b33f-402ebcfefb8d"}

;; {:data {:order-id 123456}, :reply-to nil, :headers {},
;;  :key save-as-file, :id #uuid "88750295-2d30-11e8-b33f-402ebcfefb8d"}


