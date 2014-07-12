(ns clj-jri.core)

;This is a simple Clojure wrapper for JRI, Java-to-R bridge.
;refer to https://github.com/s-u/rJava
;refer to http://www.rosuda.org/r/nightly/javadoc/org/rosuda/JRI/Rengine.html

;You have to set the following environment variables.
;(example) export R_HOME=/usr/lib/R/
;(example) export LD_LIBRARY_PATH=./lib
;You may need to install R library "JGR/rJava" (https://rforge.net/JGR/linux.html)
;on R by "install.packages('rJava')"

(defn -main[& args]
  (use 'clj-jri.sample))
