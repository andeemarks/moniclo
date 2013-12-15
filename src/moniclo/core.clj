(ns moniclo.core
  (require
    [clojure.java.jmx :as jmx]
    [moniclo.logging]
    [taoensso.timbre :as timbre]
    [cheshire.core :refer :all ]
    [clojure.string :as string]
    [clojure.tools.cli :refer [parse-opts]]
    )
  (:import (java.net InetAddress))
  (:gen-class ))

(timbre/refer-timbre)

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 1099
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-H" "--hostname HOST" "Remote host"
    :default (InetAddress/getByName "localhost")
    :default-desc "localhost"
    :parse-fn #(InetAddress/getByName %)]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["This is my program. There are many like it, but this one is mine."
        ""
        "Usage: program-name [options]"
        ""
        "Options:"
        options-summary]
    (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
    (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn monitor
  [jmx-server jmx-port]
  (info "Querying JMX server running at " jmx-server ":" jmx-port "...")
  (jmx/with-connection {:host jmx-server, :port jmx-port}
    (let [permGenUsage (:Usage (jmx/mbean "java.lang:type=MemoryPool,name=Perm Gen"))
          heapUsage (:HeapMemoryUsage (jmx/mbean "java.lang:type=Memory"))]
      (generate-string {:heap {:used (:used heapUsage)
                               :free (= (:max heapUsage) (:used heapUsage))}
                        :permGen {:used (:used permGenUsage)
                                  :free (- (:max permGenUsage) (:used permGenUsage))}}))))

(defn -main
  "Return monitoring values from a specified JMX server"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    (monitor "localhost" 1099)))

