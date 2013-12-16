(ns moniclo.monitor
  (require
    [clojure.java.jmx :as jmx]
    [taoensso.timbre :as timbre]
    [cheshire.core :refer :all ]
    [clojure.string :as string]
    [clojure.tools.cli :refer [parse-opts]]
    )
  (:gen-class ))

(timbre/refer-timbre)

(defn monitor
  [jmx-server jmx-port]
  (info (str "Querying JMX server running at " jmx-server ":" jmx-port "..."))
  (jmx/with-connection {:host jmx-server, :port jmx-port}
    (let [permGenUsage (:Usage (jmx/mbean "java.lang:type=MemoryPool,name=Perm Gen"))
          heapUsage (:HeapMemoryUsage (jmx/mbean "java.lang:type=Memory"))]
      (generate-string {:heap {:used (:used heapUsage)
                               :free (= (:max heapUsage) (:used heapUsage))}
                        :permGen {:used (:used permGenUsage)
                                  :free (- (:max permGenUsage) (:used permGenUsage))}}))))
