(ns google-drive-api-clj.actions
  (:require [google-drive-api-clj.constants :refer [drive-service]])
  (:import (com.google.api.services.drive.model File)
           (com.google.api.client.http FileContent)
           (org.apache.tika Tika)
           (java.io BufferedInputStream FileInputStream OutputStream ByteArrayOutputStream)))

(defonce ^File file-metadata (File.))
(defonce ^Tika tika (Tika.))
(defonce ^ByteArrayOutputStream output-stream (ByteArrayOutputStream.))

(defn get-id-by-name
  [name]
  (first (let [found-data (-> drive-service
                       .files
                       .list
                       (.setQ (str "name contains '" (symbol name) "'"))
                       (.setSpaces "drive")       ;; CHECK THIS!!!!
                       (.setFields "nextPageToken, files(id, name)")
                       (.setPageToken nil)
                       .execute
                       .getFiles
                       )]
    (if (empty? found-data)
      (str "No folders/files were found.")
      (map (fn [x]
             (.getId x)) found-data)))))


(defn download
  [name]
  (do (= true (-> drive-service
                  .files
                  (.get (get-id-by-name name))
                  (.executeMediaAndDownloadTo output-stream)))
      (str "File named " name " is successfully downloaded") )) ;; GDE IH CUVA!



(defn list-all
  "Action function for listing all files, folders or files and folders."
  ([]
   (let [files (-> drive-service
                   .files
                   .list
                   (.setFields "nextPageToken, files(id, name)")
                   .execute
                   .getFiles)]
     (if (empty? files)
       (println "No folders/files were found.")
       (map (fn [x]
              (let [file-name (.getName x)
                    file-id (.getId x)
                    #_mime-type #_(URLConnection/guessContentTypeFromStream (BufferedInputStream. x))]
                (str "Folder/File name: " (.getName x) " / "
                     "Folder/File ID: " (.getId x) " ... " )
                #_(if (= mime-type "application/vnd.google-apps.file")
                  (str "File name: " file-name " / "
                       "File ID: " file-id " ... " )
                  (str "Folder name: " file-name " / "
                       "Folder ID: " file-id " ... " )))) files))))
  ([type]
  (case type
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

    "folders" (let [folders (-> drive-service
                                .files
                                .list
                                (.setQ "mimeType = 'application/vnd.google-apps.folder'")
                                (.setSpaces "drive")       ;; CHECK THIS!!!!
                                (.setFields "nextPageToken, files(id, name)")
                                (.setPageToken nil)
                                .execute
                                .getFiles
                                )]
                (if (empty? folders)
                  (str "No folders were found.")
                  (map (fn [x]
                         (str "Folder name: " (.getName x) " / "
                              "Folder ID: " (.getId x) " ... " )) folders))))))



(defn create-folder
  "Action function for creating a folder."
  [folder-name]
  (do (.setName file-metadata folder-name)
      (.setMimeType file-metadata "application/vnd.google-apps.folder")
      (let [folder (-> drive-service
                       .files
                       (.create file-metadata)
                       (.setFields "id")
                       .execute)]
        (str "Successfully created folder: " folder-name))))


(defn upload-file
  "Action function for uploading a file."
  [file-name file-path]
  (do (.setName file-metadata file-name)
      (let [filePath (java.io.File. file-path)
            mime-type (.detect tika (java.io.File. file-path))
            media-content (FileContent. mime-type filePath)
            file (-> drive-service
                     .files
                     (.create file-metadata media-content)
                     (.setFields "id")
                     .execute)]
        (str "File ID: " (.getId file)))))


(defn search-for
  "Action function for searching specific file or folder"
  [name]
  (let [found-data (-> drive-service
                    .files
                    .list
                    (.setQ (str "name contains '" (symbol name) "'"))
                    (.setSpaces "drive")       ;; CHECK THIS!!!!
                    (.setFields "nextPageToken, files(id, name)")
                    (.setPageToken nil)
                    .execute
                    .getFiles
                    )]
    (if (empty? found-data)
      (str "No folders/files were found.")
      (map (fn [x]
             (str "Folder/File name: " (.getName x) " / "
                  "Folder/File ID: " (.getId x) " ... " )) found-data))))


































