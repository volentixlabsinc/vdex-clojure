(ns libvtx.common
  (:require
    [duct.logger :refer [log]]))


(defn ->db-spec
  [conf]
  (-> conf :db :spec))


(defmacro with-try
  "Wraps function call to try/catch statement."
  [fn-call conf]
  `(if (contains? #{:development :testing} (:environment ~conf))
    ~fn-call
    (try
      ~fn-call
      (catch Exception ~'e
        (log (:logger ~conf) :error ~'e)
        {:headers {"Content-Type" "text/html"}
         :body "Internal server error."
         :status 500}))))
