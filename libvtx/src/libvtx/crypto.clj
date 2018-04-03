(ns libvtx.crypto)

(defn gen-address []
  (str (java.util.UUID/randomUUID)))
