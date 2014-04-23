(ns idl-parser.tests.datatype-parser-test
  (:require [midje.sweet :refer :all]
            [instaparse.core :as insta]
            [clojure.java.io :as io]))

(facts "datatype parser"
  (let [grammar (slurp (io/resource "grammars/idl.ebnf"))
        parser (insta/parser grammar :start :DATA_TYPE)]
    (fact "parses primitive datatypes")
      (parser "void")   => [:DATA_TYPE "void"]
      (parser "bool")   => [:DATA_TYPE "bool"]
      (parser "raw")    => [:DATA_TYPE "raw"]
      (parser "string") => [:DATA_TYPE "string"]
      (parser "byte")   => [:DATA_TYPE "byte"]
      (parser "short")  => [:DATA_TYPE "short"]
      (parser "int")    => [:DATA_TYPE "int"]
      (parser "long")   => [:DATA_TYPE "long"]
      (parser "ubyte")  => [:DATA_TYPE "ubyte"]
      (parser "ushort") => [:DATA_TYPE "ushort"]
      (parser "uint")   => [:DATA_TYPE "uint"]
      (parser "ulong")  => [:DATA_TYPE "ulong"]
      (parser "float")  => [:DATA_TYPE "float"]
      (parser "double") => [:DATA_TYPE "double"]
    (fact "parses datacollection type declarations"
      (parser "list<int>")    => [:DATA_TYPE [:COLLECTION "list" [:DATA_TYPE "int"]]]
      (parser "list<float>")  => [:DATA_TYPE [:COLLECTION "list" [:DATA_TYPE "float"]]]
      (parser "map<int,int>") => [:DATA_TYPE
                                    [:COLLECTION "map"
                                      [:DATA_TYPE "int"] [:DATA_TYPE "int"]]])
    (fact "parses user defined data-types"
      (parser "user_type") => [:DATA_TYPE "user_type"]
      (parser "list<user_type2>") =>
        [:DATA_TYPE [:COLLECTION "list" [:DATA_TYPE "user_type2"]]]
      (parser "map<user_type3, user_type4>") =>
        [:DATA_TYPE
          [:COLLECTION "map"
            [:DATA_TYPE "user_type3"] [:DATA_TYPE "user_type4"]]])))
