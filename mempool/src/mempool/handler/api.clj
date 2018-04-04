(ns mempool.handler.api
  (:require
    [compojure.core :refer [routes POST]]
    [integrant.core :as ig]
    [mempool.handler.kafka-producer :as kafka-producer]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]  ))


(defmethod ig/init-key :mempool.handler/api
  [_ config]
  (routes
    (->
      (POST "/order" {:keys [body-params] :as request}
            (kafka-producer/produce-message config (:key body-params) (:msg body-params))
            {:status 201})
      (wrap-json-body {:keywords? true :bigdecimals? true}))))
