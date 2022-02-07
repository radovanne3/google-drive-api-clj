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
  (if (and (string? name) (not (nil? match-type)))
    (let [match-type (case (keyword match-type)
                       :partial  "contains"
                       :exact    "="
                       "=")
          found-data (-> drive-service
                         .files
                         .list
                         (.setQ (str "name " match-type " '" (symbol name) "'"))
                         (.setSpaces "drive")       ;; CHECK THIS!!!!
                         (.setFields "nextPageToken, files(id, name, parents, mimeType)")
                         (.setPageToken nil)
                         .execute
                         .getFiles
                         )]
      (if (empty? found-data)
        nil
        found-data))
    {:error "Error"
     :error-code :invalid-parameters})
  )


(defn get-data-using-id
  [id]
  (let [data (-> drive-service
                    .files
                    (.get id)
                    (.setFields "id, name, parents")
                    .execute
                    )]
    data
    #_{"id" "1W3VwRivqlfBXnXNCdfCeb3tHn9NsWPrr",
       "name" "test-linux-commands.pdf",
       "parents" ["1mAsa3JSdAlGNTwTLsvNZ1hFjPySzzBaC"]}
    ))


;; ACTION FUNCTIONS

(defn create-directory
  "Action function for creating a folder."
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
    (println (str "Successfully created directory:
    ID:  " (get directory "id") "
    NAME:  " (get directory "name")))))
    (println "Please provide required argument 'directory-name'.")))


(defn upload
  "Action function for uploading a file."
  [name path]
  (if (or (not (nil? name)) (not (nil? path)))
    (let [filePath (java.io.File. path)
          mime-type (mime-type-of filePath)
          media-content (FileContent. mime-type filePath)
          file-metadata (.setName (File.) name)
          file (-> drive-service
                 .files
                 (.create file-metadata media-content)
                 (.setFields "id, name")
                 .execute)]
      (println file)
      (str "File Name: " (.getName file) " / " "File ID: " (.getId file)))
    "Please provide required arguments in this order:
     file-name
     absolute-path-to-the-file"))



(defn upload-to-directory
  "Action function for uploading a file to directory.
  In case that directory doesn't exist this function will create one with the given name
  and upload file to it.."
  [directory-name file-name file-path]
  (if (and (not (nil? directory-name)) (not (nil? file-name)) (not (nil? file-path)))
    (if (string? (get (get-metadata-by-name directory-name :partial) "id"))
      (let [file-metadata (File.)
            directory-id (get (get-metadata-by-name directory-name :partial) "id")]
      (.setName file-metadata file-name)
      (.setParents file-metadata (Collections/singletonList directory-id))
      (let [filePath (java.io.File. file-path)
            mime-type (mime-type-of filePath)
            media-content (FileContent. mime-type filePath)
            file (-> drive-service
                     .files
                     (.create file-metadata media-content)
                     (.setFields "id, name, parents")
                     .execute)]
        (str "File ID " (.getId file) " is uploaded to " (get (get-data-using-id directory-id) "name") "..")))
      (do (create-directory directory-name)
          (upload-to-directory directory-name file-name file-path)
          (str "Directory " directory-name " didn't exist when this command was invoked.
                I created one and uploaded " file-name " to it.")))
    "Please provide required arguments in this order:
     directory-name
     file-name
     absolute-path-to-the-file"))


(defn delete
  "Action function for deleting file or directory."
  [name]
  (if (string? name)
    (let [file-id (get (get-metadata-by-name name :exact) "id")
        mime-type (get (get-metadata-by-name name :exact) "mimeType")]
    (if (not (nil? file-id))
      (cond
      (= mime-type "application/vnd.google-apps.folder")
            (do (-> drive-service
                        .files
                        (.delete file-id)
                        .execute)
                        (str "Directory /Name? " name " /ID? " file-id " is successfully deleted"))
      :else (do (-> drive-service
                    .files
                    (.delete file-id)
                    .execute)
                (str "File " name "
                      ID: " file-id " is successfully deleted")))
      "The name you provided doesn't match with any directory or file.")))
  "Please provide name of the directory or file you wish to delete.")


;;KAKO MI DA IZABEREMO MESTO?
(defn download
  [name]
  (if (string? (get (get-metadata-by-name name :partial) "id"))
    (with-open [output-stream (io/output-stream name)]
    (let [file-id (get (get-metadata-by-name name :partial) "id")]
      (-> drive-service
        .files
        (.get file-id)
        (.executeMediaAndDownloadTo output-stream)))
    (println output-stream)
  (str "File named " name " is successfully downloaded"))
    "The name you provided doesn't match any directory or file."))



(defn list-all
  "Action function for listing folders or files and folders."
  ([]
   (let [files (-> drive-service
                   .files
                   .list
                   (.setFields "nextPageToken, files(id, name)")
                   .execute
                   .getFiles)]
     (if (empty? files)
       (println "Storage is empty.")
       (map (fn [x]
              (let [file-name (.getName x)
                    file-id (.getId x)]
                {:file-name file-name
                 :file-id file-id}
                )) files))))
  ([type]
  (case type
    "directories" (let [directories (-> drive-service
                                        .files
                                        .list
                                        (.setQ "mimeType = 'application/vnd.google-apps.folder'")
                                        (.setSpaces "drive")       ;; CHECK THIS!!!!
                                        (.setFields "nextPageToken, files(id, name)")
                                        (.setPageToken nil)
                                        .execute
                                        .getFiles
                                        )]
                    (if (empty? directories)
                      (str "No directories were found.")
                      (map (fn [x]
                             (str "Directory name: " (.getName x) " / "
                                  "Directory ID: " (.getId x) " ... " )) directories)))

    "files" (let [files (-> drive-service
                                        .files
                                        .list
                                        (.setQ "mimeType != 'application/vnd.google-apps.folder'")
                                        (.setSpaces "drive")       ;; CHECK THIS!!!!
                                        (.setFields "nextPageToken, files(id, name)")
                                        (.setPageToken nil)
                                        .execute
                                        .getFiles
                                        )]
                    (if (empty? files)
                      (str "No files were found.")
                      (map (fn [x]
                             (str "File name: " (.getName x) " / "
                                  "File ID: " (.getId x) " ... " )) files)))
    "Provided argument doesn't meet search requirements, try to search for files or directories."

    )))


;; ZNAM KAKO CU OVO DA ISPRAVIM...
(defn search-for
  "Action function for searching specific file or folder,
  first argument is type (?file or directory) second is file-look-up-name (?text.txt)"
  [type name]
  (case type
    "file" (let [found-data (-> drive-service
                                .files
                                .list
                                (.setQ (str "name contains '" (symbol name) "' and mimeType != 'application/vnd.google-apps.folder'"))
                                (.setSpaces "drive")       ;; CHECK THIS!!!!
                                (.setFields "nextPageToken, files(id, name)")
                                (.setPageToken nil)
                                .execute
                                .getFiles
                                )]
             (if (empty? found-data)
               (str "No files were found.")
               (map (fn [x]
                      (str "File name: " (.getName x) " / "
                           "File ID: " (.getId x) " ... " )) found-data)))
    "directory" (let [found-data (-> drive-service
                       .files
                       .list
                       (.setQ (str "name contains '" (symbol name) "' and mimeType = 'application/vnd.google-apps.folder'"))
                       (.setSpaces "drive")       ;; CHECK THIS!!!!
                       (.setFields "nextPageToken, files(id, name)")
                       (.setPageToken nil)
                       .execute
                       .getFiles
                       )]
    (if (empty? found-data)
      (str "No directories were found.")
      (map (fn [x]
             (str "Directory name: " (.getName x) " / "
                  "Directory ID: " (.getId x) " ... " )) found-data)))
    "Provided argument doesn't meet search requirements, try to search for files or directories."))


;; Da li i ovde zelimo da pravimo direktorijum ako vec ne postoji?
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
                    .execute)
        ]
    (map (fn [parent]
           (.append previous-parents parent)
           (.append previous-parents ",")) (.getParents old-dir))
    (-> drive-service
        .files
        (.update file-id nil)
        (.setAddParents dir-id)
        (.setRemoveParents (str previous-parents))
        (.setFields "id, parents")
        .execute))
    "Argument (file name) or (new directory name) don't exist."))






