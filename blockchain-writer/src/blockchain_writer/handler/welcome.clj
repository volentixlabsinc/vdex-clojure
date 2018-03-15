(ns blockchain-writer.handler.welcome
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :blockchain-writer.handler/welcome [_ options]
  (constantly {:status 200 :headers {"Content-Type" "text/plain"} :body "Microservice"}))
