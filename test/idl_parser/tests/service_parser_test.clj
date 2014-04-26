(ns idl-parser.tests.service-parser-test
  (:require [midje.sweet :refer :all]
            [instaparse.core :as insta]
            [clojure.java.io :as io]))

(facts "service-parser"
  (let [grammar (slurp (io/resource "grammars/idl.ebnf"))
        parser (insta/parser grammar :start :SERVICE_BLOCK)]
    (fact "parses minimal service block correctly"
      (let [test-string (str "service TEST { }")
            result (parser test-string)]
        result =not=> nil?
        (parser "service TEST {}") =>
          [:SERVICE_BLOCK
            [:SERVICE_IDENT [:NAME_IDENT "TEST"]]]))
    (fact "parses blocks where a singular function has no args"
      (parser "service TEST {void set()}") =>
         [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
          [:SERVICE_CONTENT
            [:FUNCTION
               [:DATA_TYPE "void"]
               [:NAME_IDENT "set"]
               [:FUNCTION_ARGS]]]])
    (fact "parses service blocks with a singular function "
      (parser "service TEST {int get(1: int key)}") =>
        [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
          [:SERVICE_CONTENT
            [:FUNCTION
                [:DATA_TYPE "int"]
                [:NAME_IDENT "get"]
                [:FUNCTION_ARGS
                      [:FIELD
                            [:FIELD_IDENT "1"]
                            [:DATA_TYPE "int"]
                            [:NAME_IDENT "key"]]]]]]
      (parser "service TEST {void set(1: int key = 0)}") =>
        [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
          [:SERVICE_CONTENT
            [:FUNCTION
                [:DATA_TYPE "void"]
                [:NAME_IDENT "set"]
                [:FUNCTION_ARGS
                      [:FIELD
                            [:FIELD_IDENT "1"]
                            [:DATA_TYPE "int"]
                            [:NAME_IDENT "key"]
                            [:INIT_VALUE [:VALUE "0"]]]]]]])
    (fact "parses function with multiple arguments"
      (parser "service TEST {void set(1: int key 2: int value)}") =>
        [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
          [:SERVICE_CONTENT
              [:FUNCTION
                  [:DATA_TYPE "void"]
                  [:NAME_IDENT "set"]
                  [:FUNCTION_ARGS
                      [:FIELD
                            [:FIELD_IDENT "1"]
                            [:DATA_TYPE "int"]
                            [:NAME_IDENT "key"]]
                      [:FIELD
                            [:FIELD_IDENT "2"]
                            [:DATA_TYPE "int"]
                            [:NAME_IDENT "value"]]]]]])
    (fact "parses multiple function blocks"
      (parser "service TEST {void set(1: int key) int get(1: int key)}") =>
       [:SERVICE_BLOCK
        [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
        [:SERVICE_CONTENT
        [:FUNCTION
        [:DATA_TYPE "void"]
        [:NAME_IDENT "set"]
        [:FUNCTION_ARGS
          [:FIELD
              [:FIELD_IDENT "1"]
              [:DATA_TYPE "int"]
              [:NAME_IDENT "key"]]]]]
        [:SERVICE_CONTENT
        [:FUNCTION
        [:DATA_TYPE "int"]
        [:NAME_IDENT "get"]
        [:FUNCTION_ARGS
          [:FIELD
              [:FIELD_IDENT "1"]
              [:DATA_TYPE "int"]
              [:NAME_IDENT "key"]]]]]])
    (facts "parses inherit blocks with *"
      (parser "service TEST {inherit *}") =>
        [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
          [:SERVICE_CONTENT [:INHERIT "*"]]])
    (facts "parses inherit block with function name"
      (parser "service TEST {inherit get}") =>
        [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
          [:SERVICE_CONTENT [:INHERIT [:NAME_IDENT "get"]]]])

    (facts "parses inherit block with function"
      (parser "service TEST {inherit void set()}") =>
        [:SERVICE_BLOCK
         [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
         [:SERVICE_CONTENT
           [:INHERIT
              [:FUNCTION
                  [:DATA_TYPE "void"]
                  [:NAME_IDENT "set"]
                  [:FUNCTION_ARGS]]]]])
    (facts "parses multiple inherits blocks"
      (parser "service TEST {inherit get inherit set}") =>
        [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
          [:SERVICE_CONTENT [:INHERIT [:NAME_IDENT "get"]]]
          [:SERVICE_CONTENT [:INHERIT [:NAME_IDENT "set"]]]])
    (facts "parses blocks mixed with function and inherit"
      (parser "service TEST {void set() inherit get}") =>
        [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "TEST"]]
          [:SERVICE_CONTENT
             [:FUNCTION
                 [:DATA_TYPE "void"]
                 [:NAME_IDENT "set"]
                 [:FUNCTION_ARGS]]]
          [:SERVICE_CONTENT [:INHERIT [:NAME_IDENT "get"]]]])))
