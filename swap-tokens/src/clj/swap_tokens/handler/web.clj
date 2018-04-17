(ns swap-tokens.handler.web
  (:require
   [clojure.java.io :as io]
   [compojure.core :refer [context GET routes]]
   [compojure.route :as route]
   [integrant.core :as ig]))

(defmethod ig/init-key :swap-tokens.handler/web [_ conf]
  (routes
   (GET "/" [] (io/resource "swap_tokens/public/index.html"))
   (route/resources "/")
   (route/not-found "<h1>Not Found :(</h1>")))
