(ns libvtx.handler.api
  (:require
    [compojure.core :refer [routes context GET POST]]
    [integrant.core :as ig]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.params :refer [wrap-params]]
    [libvtx.common :refer [with-try]]
    [libvtx.transaction :refer [send-transaction receive-transactions]]))


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
                  conf))))


(defmethod ig/init-key :libvtx.handler/api
  [_ conf]
  (routes
    (->
      (transaction-routes conf)
      wrap-keyword-params
      wrap-params
      (wrap-json-body {:keywords? true :bigdecimals? true})
      wrap-json-response)))
