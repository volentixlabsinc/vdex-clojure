(ns mempool.handler.welcome
  (:require
   [compojure.core :refer [routes GET]]
   [integrant.core :as ig]))

(defmethod ig/init-key :mempool.handler/welcome [_ options]
  ;; (constantly {:status 200 :headers {"Content-Type" "text/plain"} :body "Microservice"})
  (routes 
   (GET "/" [] 
        {:status 200 :headers {"Content-Type" "text/plain"} :body "Microservice"})))
