(ns matching-engine.store
  (:require
    [clojure.data.fressian :as fress]
    [clojure.java.io :as io]
    [clojure.core.async :as async]
    [me.raynes.fs   :as fs]
    [integrant.core :as ig])
  (:import
    [java.io DataInputStream DataOutputStream
             FileInputStream FileOutputStream
             ByteArrayOutputStream ByteArrayInputStream]
    [java.nio.channels FileChannel AsynchronousFileChannel CompletionHandler]
    [java.nio ByteBuffer]
    [java.nio.file Files StandardCopyOption FileSystems Path OpenOption StandardOpenOption]))

(defn deserialize-bytes [bytes]
  (fress/read bytes :handlers (fress/associative-lookup fress/clojure-read-handlers)))

(defn serialize-bytes [bytes val]
  (let [w (fress/create-writer bytes :handlers (-> fress/clojure-write-handlers
                                                   fress/associative-lookup
                                                   fress/inheritance-lookup))]
    (fress/write-object w val)))

(defn nio-async-read [file-path]
  (AsynchronousFileChannel/open (.getPath (FileSystems/getDefault)
                                          file-path
                                          (into-array String []))
                                (into-array StandardOpenOption
                                            [StandardOpenOption/READ])))

(defn nio-completion-handle [file-path result-ch ac bb]
  (proxy [CompletionHandler] []
    (completed [res att]
      (let [bais (ByteArrayInputStream. (.array bb))]
        (try
          (->> bais
               deserialize-bytes
               (async/put! result-ch))
          (catch Exception e
            (async/put! result-ch
                        (ex-info (str "Could not read file" file-path)
                                 {:type :read-error
                                  :exception e})))
          (finally
            (async/close! result-ch)
            (.close ac)))))
    (failed [t att]
      (async/close! result-ch)
      (.close ac))))

(defn read-edn
  [result-ch dir-path file-name]
  (let [f-path (str dir-path file-name)
        f (io/file f-path)]
    (if-not (.exists f)
      (async/close! result-ch)
      (try
        (let [ac (nio-async-read f-path)
              bb (ByteBuffer/allocate (.size ac))]
          (.read ac
                 bb
                 0
                 nil
                 (nio-completion-handle f-path result-ch ac bb)))
        (catch Exception e
          (async/put! result-ch
                      (ex-info (str "Could not read file " f-path)
                               {:type :read-error
                                :exception e})))))))

(defn write-edn
  [result-ch dir-path file-name value]
  (let [f-path (str dir-path file-name)
        f   (io/file f-path)
        fos (FileOutputStream. f)
        dos (DataOutputStream. fos)
        fd  (.getFD fos)]
    (try
      (serialize-bytes dos value)
      (.flush dos)
      (.close dos)
      (catch Exception e
        (println e)
        (async/put! result-ch
                    (ex-info (str "Could not write file " f-path)
                             {:type :write-error
                              :exception e})))
      (finally
        (async/put! result-ch true)
        (.close fos)))))

(def ^:private tag :tag)

(defn dispatch-load [tag & rest] tag)
(defn dispatch-save [data & rest]
  (tag data))

(defmulti load-store! dispatch-load)
(defmulti save-store! dispatch-save)

(defn- with-tag
  ([key] {tag key})
  ([key msg]
   {tag key
    key msg}))

(defn ->order-book [order-book]
  (with-tag :order-book order-book))

(defmethod load-store! :order-book [tag dir-path file-name]
  (let [read-ch (async/chan)]
    (read-edn read-ch dir-path file-name)
    (-> read-ch async/<!! (get tag))))

(defmethod save-store! :order-book [data dir-path file-name]
  (let [save-ch (async/chan)]
    (write-edn save-ch dir-path file-name data)
    (async/<!! save-ch)))

(defprotocol IStore
  (save!   [this data file-name])
  (load!   [this tag file-name])
  (delete! [this]))

(defrecord FileStore [dir-path]
  IStore
  (save! [this data file-name]
    (save-store! data dir-path file-name))

  (load! [this tag file-name]
    (load-store! tag dir-path file-name))

  (delete! [this]
    (fs/delete-dir dir-path)))

;; available serialization formats
;; - transit (on top of JSON and MSPACK)
;;   https://github.com/cognitect/transit-format
;;   https://github.com/cognitect/transit-clj
;; - fressian (binary format)
;;   https://github.com/Datomic/fressian
;;   https://github.com/clojure/data.fressian
;;   https://www.youtube.com/watch?v=JArZqMqsaB0
;; - nippy (binary format)
;;   https://github.com/ptaoussanis/nippy
;; - records serialization for transit and fressian
;;   https://github.com/replikativ/incognito
;; - proto-buffer/json-rpc
;;   used in bisq

;; fastest files save/load -> use java NIO instead of standard IO
(defmethod ig/init-key ::file [_ {:keys [dir-path] :as conf}]
  (fs/mkdir dir-path)
  (map->FileStore conf))
