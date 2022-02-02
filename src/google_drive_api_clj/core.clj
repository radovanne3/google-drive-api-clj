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
  (:gen-class))








(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))




