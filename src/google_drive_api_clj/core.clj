(ns google-drive-api-clj.core
  (:import [com.google.api.client.auth.oauth2 Credential]
           [com.google.api.client.extensions.java6.auth.oauth2 AuthorizationCodeInstalledApp]
           [com.google.api.client.extensions.jetty.auth.oauth2 LocalServerReceiver]
           [com.google.api.client.googleapis.auth.oauth2 GoogleAuthorizationCodeFlow GoogleClientSecrets]
           [com.google.api.client.googleapis.javanet GoogleNetHttpTransport]
           [com.google.api.client.http.javanet NetHttpTransport]
           [com.google.api.client.json JsonFactory]
           [com.google.api.client.json.gson GsonFactory]
           [com.google.api.client.util.store FileDataStoreFactory]
           [com.google.api.services.drive Drive DriveScopes]
           [com.google.api.services.drive.model File FileList]
           )
  (:gen-class))




(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


