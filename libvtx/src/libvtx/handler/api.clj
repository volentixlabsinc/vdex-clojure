(ns libvtx.handler.api
  (:require
    [compojure.core :refer [routes context GET POST]]
    [integrant.core :as ig]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [libvtx.common :refer [with-try]]
    [libvtx.transaction :refer [send-transaction]]))


(defn- transaction-routes
  [conf]
  (context "/transaction" []
           (POST "/" [:as request]
                 (with-try
                   (send-transaction request conf)
                   conf))))


(defmethod ig/init-key :libvtx.handler/api
  [_ conf]
  (routes
    (->
      (transaction-routes conf)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      wrap-json-response)))
