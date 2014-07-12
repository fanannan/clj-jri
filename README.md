# clj-jri

A simple wrapper library for JRI, Java/R interface, mostly to draw charts on R.


## Preparation

You may need to install R library "JGR/rJava" (https://rforge.net/JGR/linux.html) on R by "install.packages('rJava')" and have to set the following environment variables.

(example)
export R_HOME=/usr/lib/R/
export LD_LIBRARY_PATH=./lib

You also need to place REngine.jar, JRI.jar and libjri.so from the original project site onto LD_LIBRARY_PATH.

To install the library locally, the following process is recommended.

lein clean
lein deps
lein compile
lein jar
lein uberjar
lein localrepo install ./target/clj-jri-0.0.1-standalone.jar clj-jri 0.0.1-standalone


## Examples and notes

See clj-jri.sample.


## References

For detail, refer to https://github.com/s-u/rJava and to http://www.rosuda.org/r/nightly/javadoc/org/rosuda/JRI/Rengine.html


## License

Copyright © 2014 Takahiro SAWADA

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
