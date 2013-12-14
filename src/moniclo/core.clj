(ns moniclo.core
  (require
    [clojure.java.jmx :as jmx]
    [moniclo.logging]
    [taoensso.timbre :as timbre]
    [cheshire.core :refer :all ]
    )
  (:gen-class ))

(timbre/refer-timbre)

(defn -main
  "Return monitoring values from a specified JMX server"
  [jmx-server jmx-port]
  (info "Querying JMX server running at " jmx-server ":" jmx-port "...")
  (jmx/with-connection {:host jmx-server, :port jmx-port}
    (let [permGenUsage (:Usage (jmx/mbean "java.lang:type=MemoryPool,name=Perm Gen"))
          heapUsage (:HeapMemoryUsage (jmx/mbean "java.lang:type=Memory"))]
      (generate-string {:heap {:used (:used heapUsage)
                               :free (= (:max heapUsage) (:used heapUsage))}
                        :permGen {:used (:used permGenUsage)
                                  :free (- (:max permGenUsage) (:used permGenUsage))}}))))