(ns google-drive-api-clj.cli-matic
  (:require [google-drive-api-clj.actions :as a]
            [clojure.pprint :as p]))

;; region callings for cli-matic

(defn delete
  [{name :n}]
  "DELETE"
  (p/pprint (a/delete name)))


(defn search-by-type
  [{file-name :n type :t}]
  (if file-name
    (p/pprint (a/search-by-type type file-name))
    (p/pprint (a/search-by-type type))))

(defn upload
  "UPLOAD"
  [{file-name :n path :p}]
  (p/pprint (a/upload file-name path)))

(defn move-file
  "MOVE FILE"
  [{file-name :n directory-name :d}]
  (p/pprint (a/move-file file-name directory-name)))


(defn search-by-content
  [{search-level :l arguments :_arguments}]
  (p/pprint (a/search-by-content search-level arguments)))

(defn download
  "DOWNLOAD"
  [{name :n path :p}]
  (p/pprint (a/download name path)))

(defn create-directory
  "CREATE DIRECTORY"
  [{file-name :n}]
  (p/pprint (a/create-directory file-name)))

(defn upload-to-directory
  "UPLOAD TO DIRECTORY"
  [{file-name :n directory-name :d path :p}]
  (p/pprint (a/upload-to-directory directory-name file-name path)))

(defn rename
  "UPDATE NAME"
  [{old-file-name :o new-file-name :n}]
  (p/pprint (a/rename old-file-name new-file-name)))

(defn update-description
  "UPDATE DESCRIPTION"
  [{file-name :n arguments :_arguments}]
  (p/pprint (a/update-description file-name arguments)))

(defn update-properties
  "UPDATE DESCRIPTION"
  [{file-name :n arguments :_arguments}]
  (p/pprint (a/update-properties file-name arguments)))

(defn add-credentials-wrapper
  "Used to update path to credentials file before calling any command through the CLI."
  [target-function]
  (fn [{credentials-file-path :cfp :as arguments}]
    (when credentials-file-path
      (a/set-credentials-file-path! credentials-file-path))
    (target-function arguments)))
;; endregion
