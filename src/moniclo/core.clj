(ns moniclo.core
  (require
    [clojure.java.jmx :as jmx]
    [moniclo.logging]
    [moniclo.monitor :as monitor]
    [taoensso.timbre :as timbre]
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
  (->> ["MoniClo. A JMX MBean monitor implemented in Clojure."
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

(defn -main
  "Return monitoring values from a specified JMX server"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count options) 2) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    (monitor/monitor (.getHostAddress (:hostname options)) (:port options))))

