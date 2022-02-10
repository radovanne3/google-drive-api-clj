(ns google-drive-api-clj.core
  (:require [google-drive-api-clj.actions :refer :all]
            [clojure.tools.cli :refer :all])
  (:gen-class)
  )

(def cli-options
  ;; An option with a required argument
  [["-c" "--command" "Action to be invoked on google drive"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [arguments (parse-opts args cli-options)
        options (:options arguments)]
    (if (:help options)
      (println (:summary arguments))
      (let [result (search (by-type "files"))]
        (for [r result]
          (println r))))))

(-main)











