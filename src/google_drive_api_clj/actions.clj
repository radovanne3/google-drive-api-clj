(ns google-drive-api-clj.actions
  (:require [google-drive-api-clj.constants :refer [drive-service]]
            [pantomime.mime :refer [mime-type-of]])
  (:import (com.google.api.services.drive.model File)
           (com.google.api.client.http FileContent)
           (org.apache.tika Tika)
           (java.io BufferedInputStream FileInputStream OutputStream ByteArrayOutputStream)
           (java.util Collections)))

(defonce ^File file-metadata (File.))
(defonce ^Tika tika (Tika.))

;; keyword pre PROVERITI

;; HELPER FUNCTION

;; POKUSACU DA NADJEM NEKU DOKUMENTACIJU ZA GET-MIMETYPE PA DA NAMESTIM PREPOZNAVANJE DIREKTORIJUMA ILI FAJLA
#_(defn get-mimetype-by-name
    [name]
    (first (let [files (-> drive-service
                                .files
                                .list
                                (.setQ (str "name contains '" (symbol name) "'"))
                                (.setSpaces "drive")       ;; CHECK THIS!!!!
                                (.setFields "nextPageToken, files(id, name)")
                                (.setPageToken nil)
                                .execute
                                .getMimeType
                                )]
             (if (empty? files)
               (str "No folders/files were found.")
               (map (fn [x]
                      x) files)))))

;; RADI PROVERENO
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


;; RADI PROVERENO
(defn check-parent-id
  [file-name]
  (let [file-id (get-id-by-name file-name)
        parents (-> drive-service
                    .files
                    (.get file-id)
                    (.setFields "id, name, parents")
                    .execute
                    .getParents)]
    (map (fn [parent]
           parent) parents)
    ))


;; RADI PROVERENO
(defn create-directory
  "Action function for creating a folder."
  [name]
  (.setName file-metadata name)
  (.setMimeType file-metadata "application/vnd.google-apps.folder")
  (let [folder (-> drive-service
                   .files
                   (.create file-metadata)
                   (.setFields "id")
                   .execute)]
    (str "Successfully created directory: " name)))


;; GOOGLE DRIVE API NE PODRZAVA UPLOAD DIREKTORIJUMA, MORAJU SE NAPRAVITI MANUELNO (OVO NESTO NIJE KAKO TREBA)

(defn upload
  "Action function for uploading a file."
  [name path]
  (.setName file-metadata name)
  (let [filePath (java.io.File. path)
        mime-type (.detect tika (java.io.File. path))
        media-content (FileContent. mime-type filePath)
        file (-> drive-service
                 .files
                 (.create file-metadata media-content)
                 (.setFields "id")
                 .execute)]
    (str "File ID: " (.getId file))))


;; AKO DIREKTORIJUM NE POSTOJI MOZEMO DA GA NAPRAVIMO? lako se napravi, da li nam treba?
;; RADI, ALI MORA DA SE PAZI KADA SE UPISUJE PATH, MIME-TYPE MORA DA SE PODUDARA SA TIPOVIMA
;; KOJE GOOGLE API DRIVE PODRZAVA
(defn upload-to-directory
  "Action function for uploading a file or files to directory"
  [directory-name file-name file-path]
  (let [directory-id (get-id-by-name directory-name)]
    (.setName file-metadata file-name)
    (.setParents file-metadata (Collections/singletonList directory-id))
    (let [filePath (java.io.File. file-path)
          mime-type (.detect tika (java.io.File. file-path))
          media-content (FileContent. mime-type filePath)
          file (-> drive-service
                   .files
                   (.create file-metadata media-content)
                   (.setFields "id, parents")
                   .execute)]
      (str "File ID " (.getId file) " is uploaded to " directory-name ".."))))


;; RADI PROVERENO
;; KASNIJE UBACITI CHECK-UP AKO NPR IME ILI ID NE POSTOJE DA IZBACI OBJASNJENJE
(defn delete
  "Action function for deleting file or directory."
  [name]
  (let [file-id (get-id-by-name name)
        mime-type (mime-type-of (.getBytes name))]
    (if (not= file-id \N)
      (cond
      (= mime-type "application/vnd.google-apps.folder") (do (-> drive-service
                        .files
                        (.delete file-id)
                        .execute)
                        (str "Directory /Name? " name " /ID? " file-id " is successfully deleted"))
      :else (do (-> drive-service
                    .files
                    (.delete file-id)
                    .execute)
                (str "File /Name? " name " /ID? " file-id " is successfully deleted")))
      (str "DOESNT EXIST"))))


;; GDE IH CUVA I KAKO MI DA IZABEREMO MESTO!!!
;; GOOGLE DRIVE API NE PODRZAVA SKIDANJE DIREKTORIJUMA, SAMO FAJLOVA
;; NE RADI? FAJL MORA DA IMA KONTENT DA BI MOGAO DA SE SKINE
(defn download
  [name]
  (let [output-stream (new ByteArrayOutputStream)
        file-id (get-id-by-name name)]
    (-> drive-service
      .files
      (.get file-id)
      (.executeMediaAndDownloadTo output-stream))
  (str "File named " name " is successfully downloaded")))



;; RADI PROVERENO, ZELIM DA PREPOZNA DA LI JE FAJL ILI DIREKTORIJUM KADA SE "SVE" IZLISTAVA
;; Directories/Files mozda izgleda malo glupo
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
       (println "No directories/files were found.")
       (map (fn [x]
              (let [file-name (.getName x)
                    file-id (.getId x)]
                (str "Directory/File name: " file-name " / "
                     "Directory/File ID: " file-id " ... " ))) files))))
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

    )))

;; RADI PROVERENO, PRETRAGA POMOCU IMENA, MOGU SE DODATI JOS 16 NACINA (KOJI NAM TREBAJU?)
(defn search-for
  "Action function for searching specific file or folder"
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
               (str "No directories were found.")
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
                  "Directory ID: " (.getId x) " ... " )) found-data)))))

;; RADI, PROVERENO
(defn move-file
  "Action function for moving file from one directory to another.."
  [file-name new-dir-name]
  (let [file-id (get-id-by-name file-name)
        dir-id (get-id-by-name new-dir-name)
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
        .execute)))





