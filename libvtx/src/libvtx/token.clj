(ns libvtx.token
  (:require
    [rop.core :as rop]
    [libvtx.common :refer [validate-params ->db-spec]]
    [libvtx.db.db :as db]
    [libvtx.schemas :refer [token-schema]]))


(defn- =validate-token-params=
  [result]
  (validate-params result token-schema))


(defn- =create-token=
  [{:keys [params conf] :as result}]
  (db/create-token (->db-spec conf) params)
  (-> result
      (assoc :token params)
      (assoc-in [:response :status] 201)))


(defn- get-token-name
  [address conf]
  (-> conf ->db-spec (db/get-token {:address address}) :name))


(defn- =create-pairs=
  [{:keys [params conf] :as result}]
  (doseq [pair (:pairs-with params)]
    (let [pair-token-name (get-token-name pair conf)]
      (db/create-pair (->db-spec conf) {:address (:address params)
                                        :pair pair
                                        :pair-name (->> [(:name params) pair-token-name] sort (apply str))}))))


(defn create-token
  [request conf]
  (rop/>>=*
    :token
    {:params (:body-params request)
     :conf conf}
    =validate-token-params=
    (rop/switch =create-token=)
    (rop/dead =create-pairs=)))
