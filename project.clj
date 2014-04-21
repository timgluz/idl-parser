(defproject idl-parser "0.1.0-SNAPSHOT"
  :description "Jubatus&Thrift IDL parser"
  :url "http://github.com/timgluz/idl-parser"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [prismatic/schema "0.2.1"]
                 [instaparse "1.3.1"]
                 [midje "1.6.3"]]
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev
              {:dependencies [[midje "1.6.3"]]}})
