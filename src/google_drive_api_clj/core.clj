(ns google-drive-api-clj.core
  (:require [google-drive-api-clj.actions :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [cli-matic.core :refer [run-cmd]])
  (:gen-class)
  )

;; OLD WAY

#_(def cli-options
  ;; An option with a required argument
  [["-c" "--command COMMAND" "Action to be invoked on google drive"]
   ["-b" "--criteria CRITERIA" "Criteria for search-by-criteria function"]
   ["-t" "--file-type FILE_TYPE" "Type of object to search for (file or directory)"]
   ["-n" "--name NAME" "Type of object to search for (file or directory)"]
   ["-h" "--help"]])

#_(defn -main [& args]
    (let [arguments (parse-opts args cli-options)
          options (:options arguments)]
      (if (:help options)
        (println (:summary arguments))
        (cond
          (and (= (:command options) "search-by-type") (:file-type options) (:name options)) (doto (search-by-type (:file-type options) (:name options)) println)
          (and (= (:command options) "search-by-type") (:file-type options)) (doto (search-by-type (:file-type options)) println)
          )
        )))

;; TOOLS CLI PREPORUCUJE DA SE KORISI CLI-MATIC, NE DOBIJAM POVRATNU INFORMACIJU U TERMINALU KADA POKRENEM
(def configuration {:command     "google-drive-clj"
                    :description "A command-line application for working with Google Drive."
                    :version     "0.0.1"
                    :subcommands [{:command     "search-by-type"
                                   :description "Enters search state, must have
                                   additional arguments."
                                   :examples    ["search-by-tipe files"
                                                 "search-by-tipe directories"
                                                 "search-by-tipe (files or directories) (file or directory name)"
                                                 "search-by-content :level This is some text"
                                                 ":level can be:"
                                                 ":full-text -> File must contain supplied words in that order."
                                                 ":contains-any -> File must contain any of supplied words."
                                                 ":contains-every -> File must contain every of supplied words, order is not important."]
                                   :opts        [{:as     ["What type are you searching for?"
                                                           "Options are:"
                                                           "1. files"
                                                           "2. directories"]
                                                  :option "t"
                                                  :type   :string}
                                                 {:as     ["Name of file or directory."
                                                           "If this parameter is not supplied,"
                                                           "your search will be global for supplied type."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        search-by-type}
                                  {:command     "upload"
                                   :description "Uploads a file to google drive."
                                   :examples    ["upload --n name --p absolute-path"]
                                   :opts        [{:as     ["Absolute path to the file."]
                                                  :option "p"
                                                  :type   :string}
                                                 {:as     ["Name of file you wish to move."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        upl}
                                  {:command     "delete"
                                   :description "Deletes a file or directory,
                                   if directory is deleted all content is deleted too."
                                   :examples    ["delete name-of-file-or-dir"]
                                   :opts        [{:as     ["What is the name of a file or directory? "]
                                                  :type   :string
                                                  :option "n"}]
                                   :runs        del}
                                  {:command     "move-file"
                                   :description "Moves a file from one directory to another.
                                   Requires file name and new directory name arguments"
                                   :examples    ["move-file --n name-of-file --d name-of-dir"]
                                   :opts        [{:as     ["Name of the directory you wish to move file to.."]
                                                  :option "d"
                                                  :type   :string}
                                                 {:as     ["Name of file you wish to move."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        mo-fi}
                                  ]})

;; TOOLS CLI PREPORUCUJE DA SE KORISI CLI-MATIC, NE DOBIJAM POVRATNU INFORMACIJU U TERMINALU KADA POKRENEM (run-cmd args configuration) A TREBALO BI DA RADI.....
(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  (run-cmd args configuration))













