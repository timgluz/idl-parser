(ns idl-parser.tests.exception-parser-test
  (:require [midje.sweet :refer :all]
            [instaparse.core :as insta]
            [clojure.java.io :as io]))


(facts "exception-parser"
  (let [grammar (slurp (io/resource "grammars/idl.ebnf"))
        exception-parser (insta/parser grammar :start :EXCEPTION_BLOCK)]
    (fact "parses a minimal block of exception message"
      (exception-parser "exception ERR {}") =>
        [:EXCEPTION_BLOCK "exception" [:NAME_IDENT "ERR"]]
      (exception-parser "exception ERR < SUPER_ERR {}") =>
        [:EXCEPTION_BLOCK
          "exception"
          [:NAME_IDENT "ERR"]
          [:SUPER_CLASS [:NAME_IDENT "SUPER_ERR"]]]
      (exception-parser "exception ERR {1: string message}") =>
        [:EXCEPTION_BLOCK
          "exception"
          [:NAME_IDENT "ERR"]
          [:FIELD
             [:FIELD_IDENT "1"]
             [:DATA_TYPE "string"]
             [:NAME_IDENT "message"]]])
    (fact "parses a multiple fields"
      (exception-parser "exception ERR {1: int code 2: string msg}") =>
        [:EXCEPTION_BLOCK
          "exception"
          [:NAME_IDENT "ERR"]
          [:FIELD [:FIELD_IDENT "1"] [:DATA_TYPE "int"] [:NAME_IDENT "code"]]
          [:FIELD [:FIELD_IDENT "2"] [:DATA_TYPE "string"] [:NAME_IDENT "msg"]]])))
