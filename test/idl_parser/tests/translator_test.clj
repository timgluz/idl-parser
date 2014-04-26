(ns idl-parser.tests.translator-test
  (:require [midje.sweet :refer :all]
            [idl-parser.translator :as translator]))


(facts "capitalize-name"
  (fact "capitalizes firs character ones"
    (translator/capitalize-name "abc") => "Abc"
    (translator/capitalize-name "Abc") => "Abc")
  (fact "capitalizes each word separated by underscore"
    (translator/capitalize-name "abc_def") => "AbcDef"
    (translator/capitalize-name "status_message") => "StatusMessage"
    (translator/capitalize-name "Ab_cd_ef_gh") => "AbCdEfGh")
  (fact "keeps CamelCased names as it is"
    (translator/capitalize-name "AbcDef") => "AbcDef"))


(facts "to-schema-fields"
  (fact "builds correct list of field and datatypes"
    (str
      (translator/to-schema-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE "string"]
          [:NAME_IDENT "label"]]])) => "[label :- java.lang.String]"
    (str
      (translator/to-schema-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE "int"]
          [:NAME_IDENT "label"]]])) => "[label :- java.lang.Integer]"
    (str
      (translator/to-schema-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE "bool"]
          [:NAME_IDENT "label"]]])) => "[label :- java.lang.Boolean]")
  (fact "handles user types correctly"
    (str
      (translator/to-schema-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE "user_defined_type"]
          [:NAME_IDENT "label"]]])) => "[label :- UserDefinedType]")
  (fact "build correct examples with datacollection"
    (str
      (translator/to-schema-fields
        [[:FIELD
          [:FIELD_IDENT "0"]
          [:DATA_TYPE [:LIST [:DATA_TYPE "int"]]]
          [:NAME_IDENT "label"]]])) => "[label :- [java.lang.Integer]]"
    (str
      (translator/to-schema-fields
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

(facts "to-schema-function"
  (fact "builds proper annotated function for AST"
    (str
      (translator/to-schema-function
        [:FUNCTION
        [:DATA_TYPE "int"]
        [:NAME_IDENT "train"]
        [:FUNCTION_ARGS
          [:FIELD
            [:FIELD_IDENT "0"]
            [:DATA_TYPE [:LIST [:DATA_TYPE "int"]]]
            [:NAME_IDENT "data"]]]]))
      => "(schema.core/defn train :- java.lang.Integer [data :- [java.lang.Integer]])"))

(facts "to-service"
  (fact "parses service info from service AST"
    (str
      (translator/to-service
        [:SERVICE_BLOCK
          [:SERVICE_IDENT [:NAME_IDENT "classifier"]]
          [:FUNCTION
            [:DATA_TYPE "int"]
            [:NAME_IDENT "train"]
            [:FUNCTION_ARGS [:FIELD [:FIELD_IDENT "0"]
                            [:DATA_TYPE [:LIST [:DATA_TYPE "int"]]]
                            [:NAME_IDENT "data"]]]]
          [:FUNCTION [:DATA_TYPE "bool"] [:NAME_IDENT "clear"] [:FUNCTION_ARGS]]]))
    => "{:service \"classifier\", :version \"classifier\", :functions ((schema.core/defn train :- java.lang.Integer [data :- [java.lang.Integer]]) (schema.core/defn clear :- java.lang.Boolean [])), :inherits []}" ))

