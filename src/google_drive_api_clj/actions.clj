(ns google-drive-api-clj.actions
  (:require [google-drive-api-clj.constants :refer [drive-service]]
            [pantomime.mime :refer [mime-type-of]]
            [clojure.java.io :as io])
  (:import (com.google.api.services.drive.model File)
           (com.google.api.client.http FileContent)
           (java.util Collections)))


;; HELPER FUNCTIONS

(defn get-metadata-by-name
  "Helper function for extracting ID from Google Drive directory or file.
  match-type parameter can be :partial( check GDA docs for .setQ operation 'contains' )
  or :exact( check GDA docs for .setQ operation '=')"
  [name match-type]
  (if (and (string? name) (keyword? match-type))
    (let [match-type (case match-type
                       :partial "contains"
                       :exact "="
                       "=")
          found-data (-> drive-service
                         .files
                         .list
                         (.setQ (str "name " match-type " '" (symbol name) "'"))
                         (.setSpaces "drive")               ;; CHECK THIS!!!!
                         (.setFields "nextPageToken, files(id, name, parents, mimeType, modifiedTime, createdTime)")
                         (.setPageToken nil)
                         .execute
                         .getFiles
                         )]
      (if (empty? found-data)
        {:error      "No data was found on drive."
         :error-code :not-found}
        (first found-data)))
    {:error      "File name exactly matching provided name was not found in your drive"
     :error-code :not-found}))

;(get-metadata-by-name "asdasd" :exact)
;(get-metadata-by-name nil :partial)
;(get-metadata-by-name "new" :exact)

(defn get-data-using-id
  [id]
  (let [data (-> drive-service
                 .files
                 (.get id)
                 (.setFields "id, name, parents")
                 .execute)]
    data))


;; ACTION FUNCTIONS

(defn create-directory
  "Action function for creating a directory."
  [name]
  (if (string? name)
    (let [file-metadata (File.)]
      (.setName file-metadata name)
      (.setMimeType file-metadata "application/vnd.google-apps.folder")
      (let [directory (-> drive-service
                          .files
                          (.create file-metadata)
                          (.setFields "id, name")
                          .execute)]
        {:success         true
         :success-message (str "Successfully created directory:
    ID:  " (get directory "id") "
    NAME:  " (get directory "name"))}))
    {:error      "You must provide name for new directory"
     :error-code :not-found}))

;(create-directory nil)
;(create-directory "new-test-dir")

(defn upload
  "Action function for uploading a file."
  [name path]
  (if (and (string? name) (string? path))
    (let [file-path (java.io.File. path)
          mime-type (mime-type-of file-path)
          media-content (FileContent. mime-type file-path)
          file-metadata (.setName (File.) name)
          file (-> drive-service
                   .files
                   (.create file-metadata media-content)
                   (.setFields "id, name")
                   .execute)]
      {:success         true
       :success-message (str "File Name: " (.getName file) " / " "File ID: " (.getId file))}
      )
    {:error-code :not-found
     :error      "Please provide required arguments in this order:
     file-name
     absolute-path-to-the-file"}))

;(upload nil "/home/snorlax/Desktop/file-with-text")
;(upload "test" nil)
;(upload "test" "/home/snorlax/Desktop/file-with-text")

(defn update-name
  "Action function for changing metadata (name) of a file or directory"
  [old-name new-name]
  (if (string? new-name)
    (let [metadata (get-metadata-by-name old-name :exact)
          id (get metadata "id")
          mime-type (get metadata "mimeType")
          file-metadata (.setName (File.) new-name)]
      (if (string? id)
        (do (-> drive-service
                .files
                (.update id file-metadata)
                .execute)
            (cond
              (= mime-type "application/vnd.google-apps.folder") {:success         true
                                                                  :success-message (str "Directory's new name is " new-name " ID " id)}
              :else {:success         true
                     :success-message (str "File's new name is " new-name "
                      ID: " id)}))
        {:error-code :not-found
         :error      "The name you provided doesn't match with any directory or file."}))
    {:error-code :not-found
     :error      "Please provide valid new name."}))

;(update-name nil "new-file-name")
;(update-name "test" nil)
;(update-name "new" "new-test-123")

(defn upload-to-directory
  "Action function for uploading a file to directory.
  In case that directory doesn't exist this function will create one with the given name
  and upload file to it.."
  [directory-name file-name file-path]
  (if (and (string? directory-name) (string? file-name) (string? file-path))
    (if (string? (get (get-metadata-by-name directory-name :partial) "id"))
      (let [file-metadata (File.)
            directory-id (get (get-metadata-by-name directory-name :partial) "id")]
        (.setName file-metadata file-name)
        (.setParents file-metadata (Collections/singletonList directory-id))
        (let [file-path (java.io.File. file-path)
              mime-type (mime-type-of file-path)
              media-content (FileContent. mime-type file-path)
              file (-> drive-service
                       .files
                       (.create file-metadata media-content)
                       (.setFields "id, name, parents")
                       .execute)]
          {:success         true
           :success-message (str "File  " (.getName file) " is uploaded to " (get (get-data-using-id directory-id) "name") "..")}))
      (do (create-directory directory-name)
          (upload-to-directory directory-name file-name file-path)
          {:success         true
           :success-message (str "Directory " directory-name " didn't exist when this command was invoked.
                I created one and uploaded " file-name " to it.")}))
    {:error-code :not-found
     :error      "Please provide required arguments in this order:
     directory-name
     file-name
     absolute-path-to-the-file"}))

;(upload-to-directory nil "upd" "home/snorlax/Desktop/file-with-text")
;(upload-to-directory "upd-dir" nil "home/snorlax/Desktop/file-with-text")
;(upload-to-directory "upd-dir" "upd" nil)
;(upload-to-directory "def-not-exist-123456" "upd" "/home/snorlax/Desktop/file-with-text")
;(upload-to-directory "upd-dir" "upd1234" "/home/snorlax/Desktop/file-with-text")


(defn delete
  "Action function for deleting file or directory."
  [name]
  (if (string? name)
    (let [file-id (get (get-metadata-by-name name :exact) "id")
          mime-type (get (get-metadata-by-name name :exact) "mimeType")]
      (if (string? file-id)
        (cond
          (= mime-type "application/vnd.google-apps.folder")
          (do (-> drive-service
                  .files
                  (.delete file-id)
                  .execute)
              {:success         true
               :success-message (str "Directory " name "
                              ID " file-id " is successfully deleted")})
          :else (do (-> drive-service
                        .files
                        (.delete file-id)
                        .execute)
                    {:success         true
                     :success-message (str "File " name "
                      ID: " file-id " was successfully deleted")}))
        {:error-code :not-found
         :error      "The name you provided doesn't match with any directory or file."}))
    {:error-code :not-found
     :error      "Please provide name of the directory or file you wish to delete."}))

;(delete nil)
;(delete "asdasdas")
;(delete "upd-dir")

(defn download
  [name]
  (if (string? (get (get-metadata-by-name name :exact) "id"))
    (with-open [output-stream (io/output-stream name)]
      (let [file-id (get (get-metadata-by-name name :partial) "id")]
        (-> drive-service
            .files
            (.get file-id)
            (.executeMediaAndDownloadTo output-stream)))
      {:success         true
       :success-message (str "File named " name " is successfully downloaded")})
    {:error-code :not-found
     :error      "The name you provided doesn't match any directory or file."}))
(get (get-metadata-by-name "name" :partial) "name")

;(download nil)
;(download "asdanil")
;(download "new")

(defn search
  "Search"
  [command]
  (if (not (nil? command))
    (let [data (fn [condition]
                 (-> drive-service
                     .files
                     .list
                     (.setQ condition)
                     (.setSpaces "drive")
                     (.setFields "nextPageToken, files(id, name, mimeType)")
                     (.setPageToken nil)
                     .execute
                     .getFiles))
          return-value (fn
                         ([data] (let [type (fn [x] (cond
                                                      (= x "application/vnd.google-apps.folder") "Directory"
                                                      :else "File"))]
                                   (cond
                                     (empty? data) {:error-code :not-found
                                                    :error      "No data was found."}
                                     :else (map (fn [x]
                                                  {:success         true
                                                   :success-message (str (clojure.string/capitalize (type (.getMimeType x))) "  name: " (.getName x) " / "
                                                                         (clojure.string/capitalize (type (.getMimeType x))) " ID: " (.getId x) " ... ")}) data)))))]
      (if (string? command)
        (return-value (data command))
        (:error command)))
    {:error-code :not-found
     :error      "Please provide valid criteria for searching.."}))


(defn by-type
  "Search files and directory by type"
  ([type]
   (let [search-query (cond
                        (= type "directories") (str "mimeType = 'application/vnd.google-apps.folder'")
                        (= type "files") (str "mimeType != 'application/vnd.google-apps.folder'")
                        :else {:error-code :not-found
                               :error      "Argument provided doesn't exist, try with files or directories"})]
     search-query))
  ([type name]
   (let [search-query (cond
                        (= type "directories") (str "name = '" (symbol name) "' and mimeType = 'application/vnd.google-apps.folder'")
                        (= type "files") (str "name = '" (symbol name) "' and mimeType != 'application/vnd.google-apps.folder'")
                        :else {:error-code :not-found
                               :error      "Argument provided doesn't exist, try with files or directories"})]
     search-query)))

(defn by-content
  "Search files by content"
  [level & args]
  (let [search-query (cond
                       (and (> (count args) 0) (= level :full-text)) (str "fullText contains " "'\"" (clojure.string/join " " args) "\"'")
                       (and (> (count args) 0) (= level :contains-every)) (clojure.string/join " and " (for [x args]
                                                                                                         (reduce str ["fullText contains " "'" (symbol x) "'"])))
                       (and (> (count args) 0) (= level :contains-any)) (clojure.string/join " or " (for [x args]
                                                                                                      (reduce str ["fullText contains " "'" (symbol x) "'"])))
                       :else {:error-code :not-found
                              :error      "Argument provided doesn't exist, try to specify if your search must be :full-text, :contains-every or :contains-any,
                        and specify what words are you looking for."})]
    search-query))

;(search nil)
;(search "asdasdas")
;(search (by-type nil))
;(search (by-type "files"))
;(search (by-type "files" "new-file-1234"))
;(search (by-type "directories"))
;(search (by-type "directories" "test-for-return"))
;(search (by-content nil))
;(search (by-content "something"))
;(search (by-content :full-text "something"))
;(search (by-content :full-text "SOME"))


#_(search (by-type type))                                   ;; SVE FAJLOVE
#_(search (by-type type name))                              ;; FAJL ODREDJENOG IMENA
#_(search (by-content :full-text params))                   ;; SVI PARAMETRI CE SE SPOJITI U JEDNU RECENICU KOJU FAJL MORA DA SADRZI
#_(search (by-content :contains-any params))                ;; SVE FAJLOVE KOJI SADRZE BAR JEDNU REC IZ PARAMETARA
#_(search (by-content :contains-every params))              ;; SVE FAJLOVE KOJI SADRZE SVE PARAMETRE RAZBACANE PO TEKSTU FAJLA



(defn move-file
  "Action function for moving file from one directory to another..
  First argument is name of the file we want to move and second is new directory name"
  [file-name new-dir-name]
  (if (and (string? (get (get-metadata-by-name file-name :partial) "id"))
           (string? (get (get-metadata-by-name new-dir-name :partial) "id")))
    (let [file-id (get (get-metadata-by-name file-name :partial) "id")
          dir-id (get (get-metadata-by-name new-dir-name :partial) "id")
          ^StringBuilder previous-parents (StringBuilder.)
          old-dir (-> drive-service
                      .files
                      (.get file-id)
                      (.setFields "parents")
                      .execute)]
      (map (fn [parent]
             (.append previous-parents parent)
             (.append previous-parents ",")) (.getParents old-dir))
      (-> drive-service
          .files
          (.update file-id nil)
          (.setAddParents dir-id)
          (.setRemoveParents (str previous-parents))
          (.setFields "id, name, parents")
          .execute)
      {:success         true
       :success-message (str "File " file-name " was successfully moved to " new-dir-name ".")})
    {:error-code :not-found
     :error      "Argument (file name) or (new directory name) don't exist."}))

;(move-file nil nil)
;(move-file nil "test-dir-2")
;(move-file "adasda" "test-dir-2")
;(move-file "new" "test-dir-2")
;(move-file "new" nil)




