(ns moniclo.logging
  (require
    [clojure.string :as str]
    [taoensso.timbre :as timbre]
    ))

(defn simple-fmt-output-fn
  [{:keys [level throwable message timestamp hostname ns]}
   & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  (format "%s %s [%s] - %s%s"
    (-> level name str/upper-case) timestamp ns (or message "")
    (or (timbre/stacktrace throwable "\n" (when nofonts? {})) "")))

(def simpler-logging-output-format {
                                     :fmt-output-fn #'simple-fmt-output-fn
                                     :timestamp-pattern "HH:mm:ss"
                                     })

(timbre/merge-config! simpler-logging-output-format)
