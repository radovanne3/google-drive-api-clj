(ns google-drive-api-clj.core
  (:require [google-drive-api-clj.cli-matic :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [cli-matic.core :refer [run-cmd]])
  (:gen-class))

(def configuration {:command     "google-drive-clj"
                    :description "A command-line application for working with Google Drive."
                    :version     "0.0.1"
                    :opts        [{:as     ["Path to credentials.json file."]
                                   :option "cfp"
                                   :type   :string}]
                    :subcommands [{:command     "search-by-type"
                                   :description "This command initiates search by type, types can be
                                   'files' or 'directories', you can be even more specific and add file or
                                   directory name that you want to search for. If name is not provided as argument,
                                   search will be global and return all files or directories that exist in
                                   \"Shared Google Drive\" "
                                   :examples    ["search-by-type --t files"
                                                 "search-by-type --t directories"
                                                 "search-by-type --t (files or directories) --n (file or directory name)"]
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
                                   :runs        (add-credentials-wrapper search-by-type)}
                                  {:command     "search-by-content"
                                   :description "This command initiates search by content, you can specify level of search.
                                   There are three levels of search:
                                   1. full-text -> File must contain supplied words in that order, like a sentence
                                   2. contains-any -> File must contain any of supplied words, order is not important.
                                   3. contains-every -> File must contain every of supplied words, order is not important."
                                   :examples    ["search-by-content --l contains-any This is some text"
                                                 ""
                                                 ":example search-by-content --l contains-any Some text or group of words"
                                                 ""
                                                 ":level can be:"
                                                 "full-text -> File must contain supplied words in that order, like a sentence."
                                                 "contains-any -> File must contain any of supplied words, order is not important."
                                                 "contains-every -> File must contain every of supplied words, order is not important."]
                                   :opts        [{:as     ["Level criteria for search function"
                                                           "There are three types of search level."
                                                           "1. full-text"
                                                           "2. contains-any"
                                                           "3. contains-every"]
                                                  :option "l"
                                                  :type   :keyword}]
                                   :runs        (add-credentials-wrapper search-by-content)}
                                  {:command     "upload"
                                   :description "Uploads a file to google drive."
                                   :examples    ["upload --n name --p absolute-path"]
                                   :opts        [{:as     ["Absolute path to the file."]
                                                  :option "p"
                                                  :type   :string}
                                                 {:as     ["Name of file you wish to move."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        (add-credentials-wrapper upload)}
                                  {:command     "rename"
                                   :description "Updates a file name."
                                   :examples    ["rename --o old-file-name --n name"]
                                   :opts        [{:as     ["Old name of the file."]
                                                  :option "o"
                                                  :type   :string}
                                                 {:as     ["New file name."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        (add-credentials-wrapper rename)}
                                  {:command     "update-description"
                                   :description "Updates a file metadata description."
                                   :examples    ["update-metadata --n file-name "]
                                   :opts        [{:as     ["Name of file you wish to update."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        (add-credentials-wrapper update-description)}
                                  {:command     "update-properties"
                                   :description "Updates a file metadata appProperties.
                                   appProperties are key value pairs that give additional information about your files.
                                   When you use this function be careful. This update will overwrite existing
                                   appProperties of the file.
                                    key you provide will be searched for, if it doesn't exist it will be created with value you provide,
                                    if it exists new value will be added to that key..
                                    There are some rules for this operation, before you use it we advise that you look at rules..
                                    URL: https://developers.google.com/drive/api/v3/properties"
                                   :examples    ["update-properties --n file-name key value key value key value"]
                                   :opts        [{:as     ["Name of file you wish to update."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        (add-credentials-wrapper update-properties)}
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
                                   :runs        (add-credentials-wrapper upload-to-directory)}
                                  {:command     "download"
                                   :description "Downloads a file from google drive."
                                   :examples    ["download --n name --p save-to-path"]
                                   :opts        [{:as     ["Name of file you wish to download."]
                                                  :option "n"
                                                  :type   :string}
                                                 {:as     ["Absolute path to save the file to."]
                                                  :option "p"
                                                  :type   :string}]
                                   :runs        (add-credentials-wrapper download)}
                                  {:command     "create-directory"
                                   :description "Creates directory with a given name."
                                   :examples    ["create-directory --n name"]
                                   :opts        [{:as     ["Name you wish for directory to have."]
                                                  :option "n"
                                                  :type   :string}]
                                   :runs        (add-credentials-wrapper create-directory)}
                                  {:command     "delete"
                                   :description "Deletes a file or directory,
                                   if directory is deleted all content is deleted too."
                                   :examples    ["delete --n name-of-file-or-dir"]
                                   :opts        [{:as     ["What is the name of a file or directory? "]
                                                  :type   :string
                                                  :option "n"}]
                                   :runs        (add-credentials-wrapper delete)}
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
                                   :runs        (add-credentials-wrapper move-file)}]})

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  (run-cmd args configuration))
