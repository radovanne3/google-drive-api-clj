(ns google-drive-api-clj.constants
  (:import
    (com.google.api.services.drive DriveScopes Drive Drive$Builder)
    (com.google.api.client.googleapis.javanet GoogleNetHttpTransport)
    (com.google.api.client.json.gson GsonFactory)
    (com.google.auth.http HttpCredentialsAdapter)
    (com.google.auth.oauth2 ServiceAccountCredentials)
    (java.io FileInputStream)
    (com.google.api.client.json JsonFactory)
    ))

(defonce application-name "Google Drive Api")

(defonce ^JsonFactory json-factory (GsonFactory/getDefaultInstance))

(defonce ^DriveScopes scopes (DriveScopes/DRIVE))

(def credentials-file-path "resources/credentials.json")

(defonce ^GoogleNetHttpTransport http-transport (GoogleNetHttpTransport/newTrustedTransport))

(defn ^ServiceAccountCredentials credential-with-scopes
  [^ServiceAccountCredentials credentials scopes]
  (.createScoped credentials (list scopes)))

(defn default-credential
  ([scopes]
   (credential-with-scopes (ServiceAccountCredentials/fromStream (new FileInputStream credentials-file-path)) scopes)))

(def ^HttpCredentialsAdapter request-initializer (HttpCredentialsAdapter. (default-credential scopes)))

(def ^Drive drive-service
  (-> (Drive$Builder. http-transport json-factory request-initializer)
      (.setApplicationName application-name)
      .build))

















