(ns matching-engine.handler.welcome
  (:require
   [integrant.core :as ig]))


(defmethod ig/init-key :matching-engine.handler/welcome [_ options]
  (constantly {:status 200 :headers {"Content-Type" "text/plain"} :body "Microservice"}))
