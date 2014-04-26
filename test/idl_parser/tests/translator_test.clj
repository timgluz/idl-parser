(ns idl-parser.tests.translator-test
  (:require [midje.sweet :refer :all]
            [idl-parser.translator :as translator]))


(facts "capitalize-name"
  (fact "capitalizes firs character ones"
    (translator/capitalize-name "abc") => "Abc"
    (translator/capitalize-name "Abc") => "Abc"
  (fact "capitalizes each word separated by underscore"
    (translator/capitalize-name "abc_def") => "AbcDef"
    (translator/capitalize-name "status_message") => "StatusMessage"
    (translator/capitalize-name "Ab_cd_ef_gh") => "AbCdEfGh")))


(facts translate-message-fields
  (fact "builds correct list of field and datatypes"
    (str
      (translator/translate-message-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE "string"]
          [:NAME_IDENT "label"]]])) => "[label :- java.lang.String]"
    (str
      (translator/translate-message-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE "int"]
          [:NAME_IDENT "label"]]])) => "[label :- java.lang.Integer]"
    (str
      (translator/translate-message-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE "bool"]
          [:NAME_IDENT "label"]]])) => "[label :- java.lang.Boolean]")
  (fact "handles user types correctly"
    (str
      (translator/translate-message-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE "user_defined_type"]
          [:NAME_IDENT "label"]]])) => "[label :- UserDefinedType]")
  (fact "build correct examples with datacollection"
    (str
      (translator/translate-message-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE [:LIST [:DATA_TYPE "int"]]]
          [:NAME_IDENT "label"]]])) => "[label :- [java.lang.Integer]]"
    (str
      (translator/translate-message-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE [:MAP [:DATA_TYPE "int"] [:DATA_TYPE "string"]]]
          [:NAME_IDENT "label"]]])) => "[label :- {java.lang.Integer java.lang.String}]"))

(facts "to-schema-record"
  (fact "converts properly formated message's AST into schema's record"
    (str
      (translator/to-schema-record
        [:MESSAGE_BLOCK
          [:NAME_IDENT "estimate_result"]
          [:FIELD [:FIELD_IDENT "0"] [:DATA_TYPE "string"] [:NAME_IDENT "label"]]]))
     => "(schema.core/defrecord EstimateResult [label :- java.lang.String])"
    (str
      (translator/to-schema-record
        [:MESSAGE_BLOCK
          [:NAME_IDENT "estimate_result"]
          [:FIELD [:FIELD_IDENT "0"] [:DATA_TYPE "string"] [:NAME_IDENT "key"]]
          [:FIELD [:FIELD_IDENT "0"] [:DATA_TYPE "int"] [:NAME_IDENT "value"]]]))
     => "(schema.core/defrecord EstimateResult [key :- java.lang.String value :- java.lang.Integer])"
    (str
      (translator/to-schema-record
        [:MESSAGE_BLOCK
          [:NAME_IDENT "estimate_result"]
          [:FIELD [:FIELD_IDENT "0"] [:DATA_TYPE "string"] [:NAME_IDENT "key"]]
          [:FIELD
            [:FIELD_IDENT "0"]
            [:DATA_TYPE [:LIST [:DATA_TYPE "int"]]]
            [:NAME_IDENT "value"]]]))
     => "(schema.core/defrecord EstimateResult [key :- java.lang.String value :- [java.lang.Integer]])"
   ))


