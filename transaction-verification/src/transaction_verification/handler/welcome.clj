(ns transaction-verification.handler.welcome
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :transaction-verification.handler/welcome [_ options]
  (constantly {:status 200 :headers {"Content-Type" "text/plain"} :body "Microservice"}))
