(ns google-drive-api-clj.core
  (:import [com.google.api.client.googleapis.javanet GoogleNetHttpTransport]
           [com.google.api.client.http HttpRequestInitializer]
           [com.google.api.client.http.javanet NetHttpTransport]
           [com.google.api.client.json JsonFactory]
           [com.google.api.services.drive Drive DriveScopes]
           [com.google.api.services.drive.model File FileList]
           [com.google.auth.http HttpCredentialsAdapter]
           [com.google.auth.oauth2 ServiceAccountCredentials]
           [java.io FileInputStream IOException]
           [java.security GeneralSecurityException]
           [java.util Arrays List]            ;; find replacement
           )
  (:require [google-drive-api-clj.actions :refer :all])
  (:gen-class))


(defn -main
  "Main entry function, accepts parameters and behaves accordingly"
  [& args]
  (case (nth args 0)
    "create-directory" (if (nil? (second args))
                         (println "Cant invoke function whitout arguments.
           Please refer to supplied documentation for further instructions.")
                        (create-directory (nth args 1)))))








