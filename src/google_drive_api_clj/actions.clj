(ns google-drive-api-clj.actions
  (:require [google-drive-api-clj.constants :refer [drive-service]])
  (:import (com.google.api.services.drive.model File)))

(defonce ^File file-metadata (File.))

#_(defn list-files
  "Action function for listing n number of files,
  second argument will be number of file names and file IDs you want to return."
  [n]
  (let [files (-> drive-service
                  .files
                  .list
                  (.setPageSize (int n))
                  (.setFields "nextPageToken, files(id, name)")
                  .execute
                  .getFiles)]
    (if (empty? files)
      (println "No files were found.")
      (map (fn [x]
             (str "File/Folder name: " (.getName x)
                  "File/Folder ID: " (.getId x) )) files))))

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
       (println "No files were found.")
       (map (fn [x]
              (str "File/Folder name: " (.getName x) " / "
                   "File/Folder ID: " (.getId x) " ... " )) files))))
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

(list-all "files")


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



























