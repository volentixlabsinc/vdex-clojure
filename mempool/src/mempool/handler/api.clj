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
      (POST "/order" {:keys [body] :as request}
            (kafka-producer/produce-message config (:key body) (:msg body))
            {:status 201})
      (wrap-json-body {:keywords? true :bigdecimals? true}))))
