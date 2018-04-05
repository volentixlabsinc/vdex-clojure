(ns libvtx.handler.api
  (:require
    [compojure.core :refer [routes GET]]
    [integrant.core :as ig]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))


(defmethod ig/init-key :libvtx.handler/api
  [_ config]
  (routes
    (->
      (GET "/" [] 
           {:status 200 :body "changeme"})
      (wrap-json-body {:keywords? true :bigdecimals? true})
      wrap-json-response)))
