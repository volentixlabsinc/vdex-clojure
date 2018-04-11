(ns libvtx.common
  (:require
    [clojure.data.json :as json]
    [bouncer.core :as bouncer]
    [duct.logger :refer [log]]
    [rop.core :as rop]))


(defn ->db-spec
  [conf]
  (-> conf :db :spec))


(defn str->kwjson
  [string]
  (some-> string
          str
          (json/read-str :key-fn keyword)))


(defmacro with-try
  "Wraps function call to try/catch statement."
  [fn-call conf]
  `(if (contains? #{:development :testing} (:environment ~conf))
    ~fn-call
    (try
      ~fn-call
      (catch Exception ~'e
        (println ~'e)
        (log (:logger ~conf) :error ~'e)
        {:headers {"Content-Type" "text/html"}
         :body "Internal server error."
         :status 500}))))


(defn =validate-params=
  [schema result]
  (let [[errors _] (bouncer/validate (:params result) schema)]
    (if (empty? errors)
      (rop/succeed result)
      (rop/fail {:errors errors}))))
