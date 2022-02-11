(ns google-drive-api-clj.core
  (:require [google-drive-api-clj.actions :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [cli-matic.core :refer [run-cmd]])
  (:gen-class)
  )

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
                                  {:command     "search-by-content"
                                   :description "Enters search state, must have
                                   additional arguments."
                                   :examples    ["search-by-content :level This is some text"
                                                 ":level can be:"
                                                 ":full-text -> File must contain supplied words in that order."
                                                 ":contains-any -> File must contain any of supplied words."
                                                 ":contains-every -> File must contain every of supplied words, order is not important."]
                                   :opts        [{:as     ["Level criteria for search function"]
                                                  :option "l"
                                                  :type   :keyword}
                                                 {:as     ["Parameters as content"]
                                                  :option "c"
                                                  :multiple true
                                                  :type   :string}]
                                   :runs        search-by-content}
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
                                  {:command     "update-name"
                                   :description "Updates a file name."
                                   :examples    ["update-name --o old-file-name --n name"]
                                   :opts        [{:as     ["Old name of the file."]
                                                  :option "o"
                                                  :type   :string}
                                                 {:as     ["New file name."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        upd-name}
                                  {:command     "upload-to-directory"
                                   :description "Uploads a file to google drive directory."
                                   :examples    ["upload-to-directory --n name --d dir-name --p file-absolute-path"]
                                   :opts        [{:as     ["Absolute path to the file."]
                                                  :option "p"
                                                  :type   :string}
                                                 {:as     ["Name of file you wish to move."]
                                                  :option "n"
                                                  :type   :string}
                                                 {:as     ["Name of directory you wish to upload file on."]
                                                  :option "d"
                                                  :type   :string}]
                                   :runs        up-to-dir}
                                  {:command     "download"
                                   :description "Downloads a file from google drive."
                                   :examples    ["download --n name"]
                                   :opts        [{:as     ["Name of file you wish to download."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        dload}
                                  {:command     "create-directory"
                                   :description "Creates directory with a given name."
                                   :examples    ["create-diredtory --n name"]
                                   :opts        [{:as     ["Name you wish for directory to have."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        c-dir}
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


(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  (run-cmd args configuration))













