(ns clj-jri.R)

;This is a simple Clojure wrapper for JRI, Java-to-R bridge.
;refer to https://github.com/s-u/rJava
;refer to http://www.rosuda.org/r/nightly/javadoc/org/rosuda/JRI/Rengine.html

;You have to set the following environment variables.
;(example) export R_HOME=/usr/lib/R/
;(example) export LD_LIBRARY_PATH=./lib
;You may need to install R library "JGR/rJava" (https://rforge.net/JGR/linux.html)
;on R by "install.packages('rJava')"

(defn check-env [& [verbose]]
  (when (or (when (nil? (System/getenv "R_HOME"))
              (println "Environment variable R_HOME is not set.")
              true)
            (when (nil? (System/getenv "LD_LIBRARY_PATH"))
              (println "Environment variable LD_LIBRARY_PATH is not set.")
              true))
    (throw (Exception. (str "Check your environment variables."))))
  (when verbose
    (println "JAVA_HOME:" (System/getenv "JAVA_HOME"))
    (println "R_HOME:" (System/getenv "R_HOME"))
    (println "LD_LIBRARY_PATH:" (System/getenv "LD_LIBRARY_PATH"))))

(defn load-JRI [& [verbose]]
  (when verbose
    (println "loading JRI"))
  (try
    (import '[org.rosuda.JRI Rengine REXP RMainLoopCallbacks])
    ; with REXP, ^REXP (.eval R "sqrt(36)")
    (catch Exception ex (throw (Exception. (str "Failed loading org.rosuda.JRI.Rengine: " (.getMessage ex)))))))

; REngine is a singleton. Note this code may not be suitable for multi-threaded codes.
; *R* is only for internal inspection.
(def ^:private ^:dynamic *R* (atom nil))
(def ^:private ^:dynamic *options* (atom nil))

(defn get-instance
  "returns R instance. If necessary, executes R and returns it.
   the 'options' is a map and an example is as follows;
   (get-R-instance {:verbose false, :R-options [], :conversion true})"
  [& [options]]
  (assert (or (nil? options)(map? options)))
  (let [verbose (get options :verbose false)
        R-options (into [] (concat ["--no-save"] (get options :R-options [])))
        runMainLoop false
        initialCallbacks nil]
    ;callback functions are not implemented here.
    ;refer http://www.rosuda.org/r/nightly/javadoc/org/rosuda/JRI/RMainLoopCallbacks.html
    ;http://acs.lbl.gov/NetLoggerBlog/?p=54
    (or (org.rosuda.JRI.Rengine/getMainEngine)
        (do (check-env verbose)
            (load-JRI verbose)
            (reset! *options* options)
            (reset! *R*
               (org.rosuda.JRI.Rengine. (into-array String R-options)
                                        runMainLoop initialCallbacks))
            (when-not (.waitForR ^org.rosuda.JRI.Rengine @*R*)
              (throw (Exception. "Could not start up R.")))
            @*R*))))

(defn shutdown []
  (.end ^org.rosuda.JRI.Rengine (get-instance)))

(defn get-version []
  (org.rosuda.JRI.Rengine/getVersion))

(defn convert [^org.rosuda.JRI.REXP v & [options]]
  (when v
    (let [conversion (get options :conversion true)
          raw (= conversion :raw)
          to-vec (= conversion :vec)
          to-matrix (= conversion :matrix)
          to-single (= conversion :single)]
      (if (get options :conversion true)
        (let [t (.getType v)] ; 'case' macro does not work.
          (cond
           (= t org.rosuda.JRI.REXP/XT_NULL) nil,
           (= t org.rosuda.JRI.REXP/XT_NONE) nil,
           (= t org.rosuda.JRI.REXP/XT_INT) (.asInt v),
           (= t org.rosuda.JRI.REXP/XT_DOUBLE) (.asDouble v),
           (= t org.rosuda.JRI.REXP/XT_STR) (.asString v),
           (= t org.rosuda.JRI.REXP/XT_LANG) (.asList v),
           (= t org.rosuda.JRI.REXP/XT_SYM) (.asSymbolName v),
           (= t org.rosuda.JRI.REXP/XT_BOOL) (.asBool v),
           (= t org.rosuda.JRI.REXP/XT_VECTOR) (.asVector v),
           (= t org.rosuda.JRI.REXP/XT_LIST) (.asList v)
           (= t org.rosuda.JRI.REXP/XT_FACTOR) (.asFactor v)
           ;(= t org.rosuda.JRI.REXP/XT_ARRAY_BOOL) (vec (.asBoolArray v)),
           ;org.rosuda.JRI.REXP/XT_CLOS) nil,
           ;org.rosuda.JRI.REXP/XT_ARRAY_BOOL_UA nil,
           ;org.rosuda.JRI.REXP/XT_ARRAY_BOOL_INT nil,
           ;org.rosuda.JRI.REXP/XT_UNKNOWN nil,
           ;
           (or (= t org.rosuda.JRI.REXP/XT_ARRAY_INT)
               (= t org.rosuda.JRI.REXP/XT_ARRAY_DOUBLE))
           (cond raw (.asDoubleArray v)
                 to-single (.asDouble v)
                 to-matrix (vec (.asDoubleMatrix v))
                 :else (vec (.asDoubleArray v)))
           ;
           (= t org.rosuda.JRI.REXP/XT_ARRAY_STR)
           (cond raw (.asStringArray v)
                 to-single (.asString v)
                 :else (vec (.asStringArray v)))
           :else v))
        v))))

(defn eval* [s & [options]]
  (let [v (.eval ^org.rosuda.JRI.Rengine (get-instance options) s)
        op (if (nil? options) *options* options)]
    (convert v op)))

(defn eval-vec [xs]
  (assert (coll? xs))
  (loop [remainings xs,
         result nil]
    (if (empty? remainings)
      result
      (let [exp (first remainings)
            op (second remainings)]
        (recur (rest remainings)
               (if (string? exp)
                 (if (map? op)
                   (eval* exp op)
                   (eval* exp))))))))

(defn eval [x & [options]]
  (if (vector? x)
    (eval-vec x)
    (let [sx (clojure.string/split x #"\n")]
      (last (doall (map #(eval* % options) sx))))))

(defn eval-source [^String filepath]
  (eval (format "source('%s')"
                  (-> (java.io.File. filepath) .getAbsolutePath))))

(defn boolean? [x] (or (true? x)(false? x)))
;(defn int? [x] (or (instance? Long x)(instance? Integer x)))

(defn convert-to-R-values [value]
  (cond ; String instances are not supported by JRI yet.
   (boolean? value) (org.rosuda.JRI.REXP. org.rosuda.JRI.REXP/XT_BOOL value),
   ;(int? value) (org.rosuda.JRI.REXP. org.rosuda.JRI.REXP/XT_INT value),
   (number? value) (org.rosuda.JRI.REXP. org.rosuda.JRI.REXP/XT_DOUBLE (double value)),
   ;(string? value) (org.rosuda.JRI.REXP. org.rosuda.JRI.REXP/XT_STR value),
   (every? boolean? value) (boolean-array value)
   ;(every? int? value) (int-array value)
   (every? number? value) (double-array (vec (map double value)))
   ;(every? string? value) (make-array String value)
   ))

(defn assign [var-name value]
  (assert (string? var-name))
  (let [v (convert-to-R-values value)]
    (.assign ^org.rosuda.JRI.Rengine (get-instance)
             ^String var-name
             v)
    v))
