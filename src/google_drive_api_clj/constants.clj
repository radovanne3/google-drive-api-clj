(ns google-drive-api-clj.constants
  (:import
    (com.google.api.services.drive DriveScopes Drive Drive$Builder)
    (com.google.api.client.googleapis.javanet GoogleNetHttpTransport)
    (com.google.api.client.json.gson GsonFactory)
    (com.google.auth.http HttpCredentialsAdapter)
    (com.google.auth.oauth2 ServiceAccountCredentials)
    (java.io FileInputStream)
    (com.google.api.client.json JsonFactory)))

(defonce APPLICATION_NAME "Google Drive Api")

;;private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
(defonce ^JsonFactory JSON_FACTORY (GsonFactory/getDefaultInstance))     ;; CHECK THIS!!!!

;;private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
(defonce SCOPES (DriveScopes/DRIVE))

;;private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json";
(def CREDENTIALS_FILE_PATH "resources/credentials.json")

;;final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
(defonce HTTP_TRANSPORT (GoogleNetHttpTransport/newTrustedTransport))

;;HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter
;; (ServiceAccountCredentials.fromStream(new FileInputStream(CREDENTIALS_FILE_PATH))
;; .createScoped(SCOPES)
;;       //.createDelegated("user.whose.drive.you.want.to.share@your-domain-in-gsuite.com")
;;        );

(defn ^ServiceAccountCredentials credential-with-scopes
  "Creates a copy of the given credential, with the specified scopes attached.
  `scopes` should be a list or vec of one or more Strings"
  [^ServiceAccountCredentials credentials, scopes]
  (.createScoped credentials (list scopes)))

(defn default-credential
  "Gets the default credential as configured by the GOOGLE_APPLICATION_CREDENTIALS environment variable
  (see https://developers.google.com/identity/protocols/application-default-credentials)
  Optionally you may specify a collection (list/vec/set) of string scopes to attach to the credential"
  ([scopes]
   (credential-with-scopes (ServiceAccountCredentials/fromStream (new FileInputStream CREDENTIALS_FILE_PATH)) scopes)))

(def requestInitializer (new HttpCredentialsAdapter (default-credential SCOPES)))

;;Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
;                .setApplicationName(APPLICATION_NAME)
;                .build();


(def ^Drive drive-service
  (-> (Drive$Builder. HTTP_TRANSPORT JSON_FACTORY requestInitializer)
      (.setApplicationName APPLICATION_NAME)
      .build))

(defn list-files
  "List first n files, second argument will be number of file names and file IDs you want to return"
  []
  (let [files (-> drive-service
                  .files
                  .list
                  (.setPageSize (int 10))
                  (.setFields "nextPageToken, files(id, name)")
                  .execute
                  .getFiles)]
    (if (and files (pos? (.size files)))
      (map (juxt #(.getName %) #(.getId %)) files)
      "No files or folders found")))

(list-files)

