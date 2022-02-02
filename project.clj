(defproject google-drive-api-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.google.auth/google-auth-library-oauth2-http "1.4.0"]
                 [com.google.api-client/google-api-client "1.33.1"]
                 [com.google.apis/google-api-services-drive "v3-rev20220110-1.32.1"]
                 ]
  :main ^:skip-aot google-drive-api-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})