(ns libvtx.scheduler
  (:require
    [duct.logger :refer [log]]  
    [integrant.core :as ig]
    [tick.clock :refer [now]]
    [tick.core :refer [minutes]]
    [tick.schedule]
    [tick.timeline :refer [timeline periodic-seq]]
    [libvtx.transaction :refer [mempool-transaction]]
    [libvtx.common :refer [->db-spec]]))


(def mempool-transaction-schedule (atom nil))


(defn- scheduler
  [schedule tick-unit tick-interval logger scheduled-fn]
  (let [interval (timeline (periodic-seq (now) (tick-unit tick-interval)))
        scheduled-call (try
                         (tick.schedule/schedule
                           scheduled-fn
                           interval)
                         (catch Exception e
                           (log logger :error e)))]
    (reset! schedule scheduled-call)
    (tick.schedule/start @schedule (tick.clock/clock-ticking-in-seconds))))


(defmethod ig/init-key :libvtx/scheduler
  [_ {:keys [logger mempool-interval] :as conf}]
  (scheduler mempool-transaction-schedule 
             minutes 
             mempool-interval 
             logger 
             (partial mempool-transaction conf mempool-interval)))


(defmethod ig/halt-key! :libvtx/scheduler
  [_ _]
  (tick.schedule/stop @mempool-transaction-schedule))
