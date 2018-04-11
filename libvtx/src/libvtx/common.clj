(ns libvtx.common
  (:require
    [clojure.data.json :as json]
    [clojure.string :refer [join]]
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


(defn random-string
  [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (join (repeatedly length #(char (rand-nth ascii-codes))))))
