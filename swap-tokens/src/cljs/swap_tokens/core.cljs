(ns swap-tokens.core
  (:require
   [cljs-web3.core :refer [http-provider]]
   [cljsjs.web3]
   #_[cljs.nodejs :as nodejs]
   [mount.core :as mount :refer [defstate]]))


;; (declare start)
;; (declare stop)

;; (defstate web3
;;   :start (start (merge (:web3 @config)
;;                        (:web3 (mount/args))))
;;   :stop (stop web3))

;; (def Web3 (nodejs/require "web3"))
;; (def Ganache (nodejs/require "ganache-core"))

;; (set! js/Web3 Web3)

#_(defn start [{:keys [:port :url] :as opts}]
  (let [provider (http-provider Web3 (if url url (str "http://127.0.0.1:" port)))]
    (new Web3 provider)))

#_(defn stop [web3]
  (println "web3 stopped"))

(defn ^:export init
  "The only exported function, it's called from the HTML page."
  []
  (println "== [INIT] ==")
  (let [provides-web3? (boolean (aget js/window "web3"))
        ;; web3 (if provides-web3?
        ;;        (new (aget js/window "Web3") current-provider) ;; use provider from metamask
        ;;        (web3/create-web3 (:node-url default-db)))
        ]
    (println "provides-web3?" provides-web3?)))

(comment

  (-> (mount/with-args
        {:web3 {:port 8545}})
      (mount/start))

  )
