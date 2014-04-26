(ns idl-parser.tests.message-parser-test
  (:require [midje.sweet :refer :all]
            [instaparse.core :as insta]
            [clojure.java.io :as io]))

(facts "message-parser"
  (let [grammar (slurp (io/resource "grammars/idl.ebnf"))
        msg-parser (insta/parser grammar :start :MESSAGE_BLOCK)]
    (fact "parses a minimal message blocks"
      (msg-parser "message TEST {}") =>
        [:MESSAGE_BLOCK [:NAME_IDENT "TEST"]]
      (msg-parser "message TEST < SUPER {}") =>
        [:MESSAGE_BLOCK
          [:NAME_IDENT "TEST"]
          [:SUPER_CLASS [:NAME_IDENT "SUPER"]]]
      (msg-parser "message TEST {1: string key}") =>
        [:MESSAGE_BLOCK
         [:NAME_IDENT "TEST"]
         [:FIELD
           [:FIELD_IDENT "1"]
           [:DATA_TYPE "string"]
           [:NAME_IDENT "key"]]])
    (fact "parses a multiple fields"
      (msg-parser "message TEST {1: int key 2: int value}") =>
        [:MESSAGE_BLOCK
         [:NAME_IDENT "TEST"]
         [:FIELD
           [:FIELD_IDENT "1"]
           [:DATA_TYPE "int"]
           [:NAME_IDENT "key"]]
         [:FIELD
           [:FIELD_IDENT "2"]
           [:DATA_TYPE "int"]
           [:NAME_IDENT "value"]]])))
