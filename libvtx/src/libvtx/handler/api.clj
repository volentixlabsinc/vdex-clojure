(ns libvtx.handler.api
  (:require
    [compojure.core :refer [routes context GET POST]]
    [integrant.core :as ig]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.params :refer [wrap-params]]
    [libvtx.balance :refer [read-or-create-balance]]
    [libvtx.common :refer [with-try]]
    [libvtx.token :refer [create-token]]
    [libvtx.transaction :refer [send-transaction receive-transactions transaction-confirmations]]))


(defn- transaction-routes
  [conf]
  (context "/transactions" []
           (POST "/send" [:as request]
                 (with-try
                   (send-transaction request conf)
                   conf))
           (GET "/receive" [:as request]
                (with-try
                  (receive-transactions request conf)
                  conf))
           (GET "/confirmations" [:as request]
                (with-try
                  (transaction-confirmations request conf)
                  conf))))


(defn- balance-routes
  [conf]
  (GET "/balance" [:as request]
       (with-try
         (read-or-create-balance request conf)
         conf)))


(defn- token-routes
  [conf]
  (POST "/token" [:as request]
        (with-try
          (create-token request conf)
          conf)))


(defmethod ig/init-key :libvtx.handler/api
  [_ conf]
  (-> (routes
        (transaction-routes conf)
        (balance-routes conf)
        (token-routes conf))
      wrap-keyword-params
      wrap-params
      (wrap-json-body {:keywords? true :bigdecimals? true})
      wrap-json-response))
