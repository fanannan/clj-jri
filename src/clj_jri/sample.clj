(ns clj-jri.sample
  (:require [clj-jri.R :as R]))

;As long as supported by this library, JRI basically returns a vector so that you need to specify {:conversion :single} as the second argment to get a single number from the return value from R.

;When giving {:conversion false}, then R/eval returns the REXP object without any conversion. {:conversion :vec} and {:conversion raw} gives a Clojure vector of Double values and a Java Double array respectively.

(println "# random numbers")
(println (R/eval "rnorm(5)"))
(println (R/eval "rnorm(5)" {:conversion false}))

(println "# square root")
(println (R/eval "sqrt(36)"))
(println (R/eval "sqrt(36)" {:conversion false}))
(println (R/eval "sqrt(36)" {:conversion :vec}))
(println (R/eval "sqrt(36)" {:conversion :single}))
(println (R/eval "sqrt(36)" {:conversion :raw}))

; R-expressions in a vector, multiple-R-expression with brackets in a String and line-separated R-expressions are acceptable for evaluation.
(println "# multiple statements, vector and array")
(R/eval ["a <- 120" "v <- c(10,60)" "z <- array(0, c(3,4,2))"])
(R/eval "{a <- 120; v <- c(10,60); z <- array(0, c(3,4,2));}")
(R/eval "a <- 120;
         v <- c(10,60);
         z <- array(0, c(3,4,2))")

(println (R/eval "v"))
(println (R/eval "v" {:conversion false}))
(println (R/eval "v" {:conversion :single}))
(println (R/eval "z"))

(println "# string")
(println "pwd:" (R/eval "getwd()"))

; When evaluationg an undefined function, it returns nil.
(println "# undefined function")
(println (R/eval "xxxx(a)"))

; You can assign a number vector to a varibale expressed in String but you can't assign a number and a string to the variable.
(println "# assigning vectors ")
(println (R/assign "x" [1.1 2.2 3.5]))
(println (R/eval "x"))
(println (R/assign "x" [1 2 3]))
(println (R/eval "x"))

(println "# chart test (check /tmp/LineChart.jpg)")
(println (R/eval [
            "data(cars)"
            "jpeg(file=\"/tmp/LineChart.jpg\",width=800,height=600)"
            "plot(cars, main = \"lowess(cars)\")"
            "lines(lowess(cars), col = 2)"
            "lines(lowess(cars, f = 0.2), col = 3)"
            "legend(5, 120, c(paste(\"f = \", c(\"2/3\", \".2\"))), lty = 1, col = 2:3)"
            "dev.off()"]))

(R/shutdown)

