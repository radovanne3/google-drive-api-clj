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
        {:error :not-found}
        (first found-data)))
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
            (str "Successfully created directory:
    ID:  " (get directory "id") "
    NAME:  " (get directory "name"))))
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
      (str "File Name: " (.getName file) " / " "File ID: " (.getId file)))
    "Please provide required arguments in this order:
     file-name
     absolute-path-to-the-file"))

#_(defn update-name
  "Update file name"
  [old-name new-name]
  (if (and (string? old-name) (string? new-name))
    (let [file-id (get (get-metadata-by-name old-name :exact) "id")
          mime-type (get (get-metadata-by-name old-name :exact) "mimeType")
          file-metadata (.setName (File.) new-name)]
      (if (string? file-id)
        (cond
          (= mime-type "application/vnd.google-apps.folder")
          (do (-> drive-service
                  .files
                  (.update file-id file-metadata)
                  .execute)
              (str "Directory's new name is " new-name "
                              ID " file-id))
          :else (do (-> drive-service
                        .files
                        (.update file-id file-metadata)
                        .execute)
                    (str "File's new name is " new-name "
                      ID: " file-id )))
        "The name you provided doesn't match with any directory or file."))
    "Please provide name of the directory or file you wish to delete.")
  )

#_(update-name "file-with-text" "new-file-name")

(defn update-name
  "Action function for changing metadata (name) of a file or directory"
  [old-name new-name]
  (if (string? new-name)
    (let [id (get (get-metadata-by-name old-name :exact) "id")
          mime-type (get (get-metadata-by-name old-name :exact) "mimeType")
          file-metadata (.setName (File.) new-name)]
      (if (string? id)
        (cond
          (= mime-type "application/vnd.google-apps.folder")
          (do (-> drive-service
                  .files
                  (.update id file-metadata)
                  .execute)
              (str "Directory's new name is " new-name "
                              ID " id))
          :else (do (-> drive-service
                        .files
                        (.update id file-metadata)
                        .execute)
                    (str "File's new name is " new-name "
                      ID: " id )))
        "The name you provided doesn't match with any directory or file."))
    "Please provide valid new name."))




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
    (if (string? file-id)
      (cond
      (= mime-type "application/vnd.google-apps.folder")
            (do (-> drive-service
                        .files
                        (.delete file-id)
                        .execute)
                        (str "Directory " name "
                              ID " file-id " is successfully deleted"))
      :else (do (-> drive-service
                    .files
                    (.delete file-id)
                    .execute)
                (str "File " name "
                      ID: " file-id " is successfully deleted")))
      "The name you provided doesn't match with any directory or file."))
    "Please provide name of the directory or file you wish to delete."))


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
(get (get-metadata-by-name "name" :partial) "name")


#_(defn search
  "Action function for listing files, folders, files and folders or specific file or folder."
  [type & [name]]
  (let [return-value

        condition-for-query (cond
                    (= type "directories") (str "mimeType = 'application/vnd.google-apps.folder'")
                    (= type "files") (str "mimeType != 'application/vnd.google-apps.folder'")
                    (and (string? name) (= type "files")) (str "name contains '" (symbol name) "' and mimeType != 'application/vnd.google-apps.folder'")
                    (and (string? name) (= type "directories")) (str "name contains '" (symbol name) "' and mimeType = 'application/vnd.google-apps.folder'")
               )]
    (case type
      "files" (let [found-data (-> drive-service
                                   .files
                                   .list
                                   (.setQ condition-for-query)
                                   (.setSpaces "drive")       ;; CHECK THIS!!!!
                                   (.setFields "nextPageToken, files(id, name)")
                                   (.setPageToken nil)
                                   .execute
                                   .getFiles
                                   )]
                (return-value found-data)
                )
      "directories" (let [found-data (-> drive-service
                                         .files
                                         .list
                                         (.setQ condition-for-query)
                                         (.setSpaces "drive")       ;; CHECK THIS!!!!
                                         (.setFields "nextPageToken, files(id, name)")
                                         (.setPageToken nil)
                                         .execute
                                         .getFiles
                                         )]
                      (return-value found-data))
      "Provided argument doesn't meet search requirements, try to search for files or directories.")))


;; NOVI NACIN PRETRAGE
;; search-u se dodaje nacin pretrage
(defn search
  "Search"
  [command]
  (let [data (fn [condition]
                       (-> drive-service
                                     .files
                                     .list
                                     (.setQ condition)
                                     (.setSpaces "drive")
                                     (.setFields "nextPageToken, files(id, name, mimeType)")
                                     (.setPageToken nil)
                                     .execute
                                     .getFiles
                                     ))
        return-value (fn
                       ([data] (let [type (fn [x] (cond
                                                    (= x "application/vnd.google-apps.folder") "Folder"
                                                    :else "File"))]
                                 (cond
                                 (empty? data) (str "No data was found.")
                                 :else (map (fn [x]
                                                    (str (clojure.string/capitalize (type (.getMimeType x))) "  name: " (.getName x) " / "
                                                         (clojure.string/capitalize (type (.getMimeType x))) " ID: " (.getId x) " ... " )
                                                           ) data)
                                  ))))]
    (if (string? command)
      (return-value (data command))
      (:error command))))

(defn by-type
  "Search files and folders by type"
  ([type]
  (let [search-query (cond
                       (= type "folders") (str "mimeType = 'application/vnd.google-apps.folder'")
                       (= type "files") (str "mimeType != 'application/vnd.google-apps.folder'")
                       :else {:error "Argument provided doesn't exist, try with files or folders"})]
    search-query))
  ([type name]
   (let [search-query (cond
                        (= type "folders") (str "name = '" (symbol name) "' and mimeType = 'application/vnd.google-apps.folder'")
                        (= type "files") (str "name = '" (symbol name) "' and mimeType != 'application/vnd.google-apps.folder'")
                        :else {:error "Argument provided doesn't exist, try with files or folders"})]
     search-query)))

(defn by-content
  "Search files by content"
  [exact & args]
  (let [search-query (cond
                       (and (> (count args) 0) (= exact :exact)) (str "fullText contains " "'\""(clojure.string/join " " args)"\"'")
                       (and (> (count args) 0)(= exact :not-exact)) (clojure.string/join " and " (for [x args]
                                                          (reduce str ["fullText contains " "'"(symbol x)"'"])))
                       :else {:error "Argument provided doesn't exist, try to specify if your search must be :exact or :not-exact,
                        and specify what words are you looking for."})]
    search-query)
  )

#_(search (by-type type))                                   ;; SVE FAJLOVE
#_(search (by-type type name))                              ;; FAJL ODREDJENOG IMENA
#_(search (by-content :exact params))                       ;; SVI PARAMETRI CE SE SPOJITI U JEDNU RECENICU
#_(search (by-content :not-exact params))                   ;; FAJL MORA DA SADRZI PARAMS, PARAMS MOGU BITI RAZBACANI PO FAJLU


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






