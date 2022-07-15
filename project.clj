(defproject pdf-gen-performance "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [criterium "0.4.6"]
                 [clj-pdf "2.5.8" :exclude [com.github.librepdf/openpdf]]
                 [com.github.librepdf/openpdf "1.3.27"]]
  :main pdf-gen-performance.core
  :aot [pdf-gen-performance.core]
  :repl-options {:init-ns pdf-gen-performance.core})
