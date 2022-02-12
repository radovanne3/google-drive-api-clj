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
    (p/pprint (a/search (a/search-by-type type file-name)))
    (p/pprint (a/search (a/search-by-type type)))))

(defn upload
  "UPLOAD"
  [{file-name :n path :p}]
  (p/pprint (a/upload file-name path)))

(defn move-file
  "MOVE FILE"
  [{file-name :n directory-name :d}]
  (p/pprint (a/move-file file-name directory-name)))

;;Exception: #error {
; :cause no conversion to symbol
; :via
; [{:type java.lang.IllegalArgumentException
;   :message no conversion to symbol
;   :at [clojure.core$symbol invokeStatic core.clj 598]}]
(defn search-by-content
  [{search-level :l :as arguments}]
  (p/pprint (a/search (a/search-by-content search-level arguments))))

(defn download
  "DOWNLOAD"
  [{name :n path :p}]
  (p/pprint (a/download name path)))

(defn create-directory
  "CREARE DIRECTORY"
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
;; endregion
