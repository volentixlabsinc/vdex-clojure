(ns libvtx.handler.api
  (:require
    [compojure.core :refer [routes context GET POST]]
    [integrant.core :as ig]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.params :refer [wrap-params]]
    [libvtx.common :refer [with-try]]))


(defmethod ig/init-key :libvtx.handler/api
  [_ conf]
  (-> (routes
        (GET "/" [] {:body "libVTX" :status 200}))
      wrap-keyword-params
      wrap-params
      (wrap-json-body {:keywords? true :bigdecimals? true})
      wrap-json-response))
