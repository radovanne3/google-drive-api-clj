(ns google-drive-api-clj.actions
  (:require [google-drive-api-clj.constants :refer [drive-service]]
            [pantomime.mime :refer [mime-type-of]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [google-drive-api-clj.constants :refer [credentials-file-path]])
  (:import (com.google.api.services.drive.model File)
           (com.google.api.client.http FileContent)
           (java.util Collections)))


;; HELPER FUNCTIONS

(defn- get-metadata-by-name
  "Helper function for extracting ID from a Google Drive directory or a file.
  `match-type` parameter acceptable values: :partial (check GDA docs for .setQ operation 'contains')
  or :exact (check GDA docs for .setQ operation '=')"
  [name match-type]
  (if (and (string? name) (keyword? match-type))
    (let [match-type (case match-type
                       :partial "contains"
                       :exact "="
                       "=")
          found-data (-> (drive-service)
                         .files
                         .list
                         (.setQ (str "name " match-type " '" (symbol name) "'"))
                         (.setSpaces "drive")               ;; CHECK THIS!!!!
                         (.setFields "nextPageToken, files(id, name, parents, mimeType, modifiedTime, createdTime)")
                         (.setPageToken nil)
                         .execute
                         .getFiles)]
      (if (empty? found-data)
        {:error      "No data was found on drive."
         :error-code :not-found}
        (first found-data)))
    {:error      "File name exactly matching provided name was not found in your drive"
     :error-code :not-found}))


(defn get-data-using-id
  "Gets data for a drive resource that matches the input id."
  [id]
  (let [data (-> (drive-service)
                 .files
                 (.get id)
                 (.setFields "id, name, parents")
                 .execute)]
    data))


;; region ACTION FUNCTIONS

(defn create-directory
  "Action function for creating a directory."
  [name]
  (if (string? name)
    (let [file-metadata (File.)]
      (.setName file-metadata name)
      (.setMimeType file-metadata "application/vnd.google-apps.folder")
      (let [directory (-> (drive-service)
                          .files
                          (.create file-metadata)
                          (.setFields "id, name")
                          .execute)]
        {:success         true
         :success-message (format "Successfully created directory:\nID: %s\nNAME:  %s" (get directory "id")
                                  (get directory "name"))
         :result          {:new-directory-name name}}))
    {:error      "You must provide a name for the new directory"
     :error-code :not-found}))

(defn upload
  "Action function for uploading a file."
  [name ^String path]
  (if (and (string? name) (string? path))
    (let [file-path (java.io.File. path)
          mime-type (mime-type-of file-path)
          media-content (FileContent. mime-type file-path)
          set-file-name (.setName (File.) name)
          file (-> (drive-service)
                   .files
                   (.create set-file-name media-content)
                   (.setFields "id, name")
                   .execute)]
      {:success         true
       :success-message (format "File Name: %s / File ID: %s" (.getName file) (.getId file))
       :result          {:new-file-name (.getName file) :new-file-id (.getId file)}})
    {:error-code :not-found
     :error      "Please provide required arguments in this order:\nfile-name\nabsolute-path-to-the-file"}))


(defn rename
  "Action function for changing metadata (name) of a file or directory"
  [old-name new-name]
  (if (string? new-name)
    (let [metadata (get-metadata-by-name old-name :exact)
          file-metadata (.setName (File.) new-name)
          id (get metadata "id")
          mime-type (get metadata "mimeType")
          ]
      (if (string? id)
        (do (-> (drive-service)
                .files
                (.update id file-metadata)
                .execute)
            {:success         true
             :success-message (if (= mime-type "application/vnd.google-apps.folder")
                                (format "Directory's new name is %s\nID: %s" new-name id)
                                (format "File's new name is %s\nID: %s" new-name id))
             :result          {:old-file-name old-name :new-file-name new-name}})
        {:error-code :not-found
         :error      "The name you provided doesn't match with any directory or file."}))
    {:error-code :not-found
     :error      "Please provide valid new name."}))


(defn upload-to-directory
  "Action function for uploading a file to a directory.
  In case that directory doesn't exist this function will create one with the given name
  and upload the file to it."
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
              file (-> (drive-service)
                       .files
                       (.create file-metadata media-content)
                       (.setFields "id, name, parents")
                       .execute)]
          {:success         true
           :success-message (format "File  %s  is uploaded to %s." (.getName file) (get (get-data-using-id directory-id)
                                                                                        "name"))
           :result          {:directory-name directory-name :file-name file-name :local-file-path file-path}}))
      (do (create-directory directory-name)
          (upload-to-directory directory-name file-name file-path)
          {:success         true
           :success-message (str "Directory " directory-name " didn't exist when this command was invoked.
                I created one and uploaded " file-name " to it.")}))
    {:error-code :not-found
     :error      "Please provide required arguments in this order:\ndirectory-name\nfile-nam
     absolute-path-to-the-file"}))


(defn delete
  "Action function for deleting file or directory."
  [name]
  (if (string? name)
    (let [file-id   (get (get-metadata-by-name name :exact) "id")
          mime-type (get (get-metadata-by-name name :exact) "mimeType")]
      (if (string? file-id)
        (if (= mime-type "application/vnd.google-apps.folder")
          (do (-> (drive-service)
                  .files
                  (.delete file-id)
                  .execute)
              {:success         true
               :success-message (format "Directory %s\nID %s is successfully deleted" name file-id)
               :result          {:deleted-file-name name}})
          (do (-> (drive-service)
                  .files
                  (.delete file-id)
                  .execute)
              {:success         true
               :success-message (format "File %s\nID: %s was successfully deleted" name file-id)}))
        {:error-code :not-found
         :error      "The name you provided doesn't match with any directory or file."}))
    {:error-code :not-found
     :error      "Please provide name of the directory or file you wish to delete."}))


(defn download
  [name save-to-path]
  (if (string? (get (get-metadata-by-name name :exact) "id"))
    (with-open [output-stream (io/output-stream save-to-path)]
      (let [file-id (get (get-metadata-by-name name :partial) "id")]
        (-> (drive-service)
            .files
            (.get file-id)
            (.executeMediaAndDownloadTo output-stream)))
      {:success         true
       :success-message (str "File named " name " is successfully downloaded")
       :result          {:downloaded-file-name name :output-path save-to-path}})
    {:error-code :not-found
     :error      "The name you provided doesn't match any directory or file."}))

(defn search
  "Search files and folders"
  [command]
  (if (some? command)
    (letfn [(get-data [condition]
              (-> (drive-service)
                  .files
                  .list
                  (.setQ condition)
                  (.setSpaces "drive")
                  (.setFields "nextPageToken, files(id, name, mimeType, description, appProperties)")
                  (.setPageToken nil)
                  .execute
                  .getFiles))
            (return-value [data]
              (let [type (fn [x]
                           (if (= x "application/vnd.google-apps.folder")
                             "Directory"
                             "File"))]
                (if (empty? data)
                  {:error-code :not-found
                   :error      "No data was found."}
                  {:success         true
                   :success-message (map (fn [x]
                                           (format "%s name: %s / %s ID: %s / description: %s / appProperties: %s..."
                                                   (string/capitalize (type (.getMimeType x))) (.getName x)
                                                   (string/capitalize (type (.getMimeType x))) (.getId x)
                                                   (.getDescription x) (.getAppProperties x)))
                                         data)
                   :result          data})))]
      (if (string? command)
        (return-value (get-data command))
        (:error command)))
    {:error-code :not-found
     :error      "Please provide valid criteria for searching."}))

(defn search-by-type
  "Supplement for search function"
  ([type]
   (let [search-query (cond
                        (= type "directories") (str "mimeType = 'application/vnd.google-apps.folder'")
                        (= type "files") (str "mimeType != 'application/vnd.google-apps.folder'")
                        :else {:error-code :not-found
                               :error      "Argument provided doesn't exist, try with files or directories"})]
     (search search-query)))
  ([type name]
   (let [search-query (cond
                        (= type "directories") (str "name = '" (symbol name) "' and mimeType = 'application/vnd.google-apps.folder'")
                        (= type "files") (str "name = '" (symbol name) "' and mimeType != 'application/vnd.google-apps.folder'")
                        :else {:error-code :not-found
                               :error      "Argument provided doesn't exist, try with files or directories"})]
     (search search-query))))

(defn search-by-content
  "Supplement for search function"
  [level args]
  (let [search-query (cond
                       (and (> (count args) 0) (= level :full-text))
                       (str "fullText contains " "'\"" (string/join " " args) "\"'")

                       (and (> (count args) 0) (= level :contains-every))
                       (string/join " and " (for [x args]
                                              (reduce str ["fullText contains " "'" (symbol x) "'"])))

                       (and (> (count args) 0) (= level :contains-any))
                       (string/join " or " (for [x args]
                                             (reduce str ["fullText contains " "'" (symbol x) "'"])))

                       :else {:error-code :not-found
                              :error      (str "Argument provided doesn't exist, try to specify if your search should "
                                               "be :full-text, :contains-every or :contains-any, and specify which "
                                               "words you are looking for.")})]
    (search search-query)))




(defn move-file
  "Action function for moving file from one directory to another.
  First argument is name of the file we want to move and second is new directory's name."
  [file-name new-dir-name]
  (let [file-id      (get (get-metadata-by-name file-name :partial) "id")
        directory-id (get (get-metadata-by-name new-dir-name :partial) "id")]
    (if (and (string? file-id) (string? directory-id))
      (let [previous-parents (StringBuilder.)
            old-dir          (-> (drive-service)
                                 .files
                                 (.get file-id)
                                 (.setFields "parents")
                                 .execute)]
        (map (fn [parent]
               (.append previous-parents parent)
               (.append previous-parents ","))
             (.getParents old-dir))
        (-> (drive-service)
            .files
            (.update file-id nil)
            (.setAddParents directory-id)
            (.setRemoveParents (str previous-parents))
            (.setFields "id, name, parents")
            .execute)
        {:success         true
         :success-message (str "File " file-name " was successfully moved to " new-dir-name ".")
         :result          {:new-file-name file-name}})
      {:error-code :not-found
       :error      "Argument (file name) or (new directory name) don't exist."})))

(defn update-description
  "Function for updating metadata of a file"
  [name args]
  (if (string? name)
    (let [metadata (get-metadata-by-name name :exact)
          description (string/join " " args)
          file-metadata (.setDescription (File.) description)
          id (get metadata "id")
          mime-type (get metadata "mimeType")
          ]
      (if (string? id)
        (do (-> (drive-service)
                .files
                (.update id file-metadata)
                .execute)
            {:success         true
             :success-message (if (= mime-type "application/vnd.google-apps.folder")
                                (format "Directory's metadata/description is updated to: %s" description)
                                (format "File's metadata/description is updated to: %s" description))
             :result          {:file-description description}})
        {:error-code :not-found
         :error      "The name you provided doesn't match with any directory or file."}))
    {:error-code :not-found
     :error      "Please provide valid description."}))


(defn update-properties
  "Function for updating metadata of a file"
  [name args]
  (if (string? name)
    (let [metadata (get-metadata-by-name name :exact)
          id (get metadata "id")
          mime-type (get metadata "mimeType")
          properties (string/join " " args)
          app-properties-map (into {} (map vec) (partition 2 args))
          file-metadata (.setAppProperties (File.) app-properties-map)]
      (if (string? id)
        (do
          (-> (drive-service)
              .files
              (.update id file-metadata)
              .execute)
          {:success         true
           :success-message (if (= mime-type "application/vnd.google-apps.folder")
                              (format "Directory's metadata/appProperties is updated to: %s" properties)
                              (format "File's metadata/appProperties is updated to: %s" properties))
           :result          {:updated-properties app-properties-map}})
        {:error-code :not-found
         :error      "The name you provided doesn't match with any directory or file."}))
    {:error-code :not-found
     :error      "Please provide valid properties."}))
;; endregion

;; app setup

(defn set-credentials-file-path!
  [path-to-credentials]
  (reset! credentials-file-path path-to-credentials))
