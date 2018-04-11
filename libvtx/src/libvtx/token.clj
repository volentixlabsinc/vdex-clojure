(ns libvtx.token
  (:require
    [rop.core :as rop]
    [libvtx.common :refer [=validate-params= ->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [token-schema]]))


(defn- =create-token=
  [{:keys [params db-spec] :as result}]
  (db/create-token db-spec params))


(defn- get-token-name
  [address db-spec]
  (-> db-spec (db/get-token {:address address}) :name))


(defn- =create-pairs=
  [{:keys [params db-spec] :as result}]
  (doseq [pair (:pairs-with params)]
    (let [pair-token-name (get-token-name pair db-spec)]
      (db/create-pair db-spec {:address (:address params)
                                        :pair pair
                                        :pair-name (->> [(:name params) pair-token-name] sort (apply str))})))
  params)


(defn create-token
  [db-spec token]
  (rop/>>=
    {:params token
     :db-spec db-spec}
    (partial =validate-params= token-schema)
    (rop/dead =create-token=)
    (rop/switch =create-pairs=)))
