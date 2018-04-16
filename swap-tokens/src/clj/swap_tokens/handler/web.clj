(ns swap-tokens.handler.web
  (:require
   [clojure.java.io :as io]
   [compojure.core :refer [context GET routes]]
   [integrant.core :as ig]))

(defmethod ig/init-key :swap-tokens.handler/web [_ conf]
  (routes
   (GET "/" [] (io/resource "swap_tokens/public/index.html"))))
