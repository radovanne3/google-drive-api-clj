(ns google-drive-api-clj.cli-matic
  (:require [google-drive-api-clj.actions :as a]
            [clojure.pprint :as p]))

;; callings for cli-matic

(defn delete
  [{name :n :as _arguments}]
  (p/pprint (a/delete name)))

(defn search-by-type
  ([{file-name :n :as _arguments}]
   (if file-name
     (p/pprint (a/search (a/search-by-type (:t _arguments) (:n _arguments))))
     (p/pprint (a/search (a/search-by-type (:t _arguments)))))))

(defn search-by-content
  [_arguments]
  (p/pprint (a/search (a/search-by-content (:l _arguments) (:_arguments _arguments)))))

(defn upload
  [_arguments]
  (p/pprint (a/upload (:n _arguments) (:p _arguments))))

(defn move-file
  [_arguments]
  (p/pprint (a/move-file (:n _arguments) (:d _arguments))))


(defn download
  [{name :n :as _arguments}]
  (p/pprint (a/download name)))

(defn create-directory
  [{name :n :as _arguments}]
  (p/pprint (a/create-directory name)))

(defn upload-to-directory
  [_arguments]
  (p/pprint (a/upload-to-directory (:d _arguments) (:n _arguments) (:p _arguments))))

(defn update-name
  [_arguments]
  (p/pprint (a/update-name (:o _arguments) (:n _arguments))))
