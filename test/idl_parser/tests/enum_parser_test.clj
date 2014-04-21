(ns idl-parser.tests.enum-parser-test
  (:require [midje.sweet :refer :all]
            [instaparse.core :as insta]
            [clojure.java.io :as io]))

(facts "enum-parser"
  (let [grammar (slurp (io/resource "grammars/idl.ebnf"))
        enum-parser (insta/parser grammar :start :ENUM_BLOCK)]
    (fact "parses a minimal block of enum"
      (enum-parser "enum TEST {}") =>
        [:ENUM_BLOCK "enum" [:NAME_IDENT "TEST"]]
      (enum-parser "enum TEST {1: RED}") =>
        [:ENUM_BLOCK
          "enum"
          [:NAME_IDENT "TEST"]
          [:ENUM_FIELD [:FIELD_IDENT "1"] [:NAME_IDENT "RED"]]])
    (fact "parses a multiple fields"
      (enum-parser "enum TEST {1: RED 2: BLUE}") =>
        [:ENUM_BLOCK
          "enum"
          [:NAME_IDENT "TEST"]
          [:ENUM_FIELD [:FIELD_IDENT "1"] [:NAME_IDENT "RED"]]
          [:ENUM_FIELD [:FIELD_IDENT "2"] [:NAME_IDENT "BLUE"]]])))

