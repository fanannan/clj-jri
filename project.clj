(defproject clj-jri "0.1.0-SNAPSHOT"
  :description "Wrapper for Java/R interface"
  :url "https://github.com/fanannan/clj-jri"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :resource-paths [;"./lib/RserveEngine.jar"
                   "./lib/REngine.jar"
                   "./lib/JRI.jar"
                   "./lib/libjri.so"
                   ]
  :main clj-jri.core)

;lein clean; lein deps; lein compile; lein jar; lein uberjar; lein localrepo install ./target/clj-jri-0.0.1-standalone.jar clj-jri 0.0.1-standalone;
