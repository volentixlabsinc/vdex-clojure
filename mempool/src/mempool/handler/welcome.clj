(ns mempool.handler.welcome
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :mempool.handler/welcome [_ options]
  (constantly {:status 200 :headers {"Content-Type" "text/plain"} :body "Microservice"}))
