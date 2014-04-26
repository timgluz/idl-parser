(ns idl-parser.translator
  (:require [clojure.zip :as zip]
            [clojure.string :as string]
            [schema.core :as schema]))

(def datatypes {"bool" java.lang.Boolean
                "byte" java.lang.Byte
                "int"  java.lang.Integer
                "long" java.lang.Long
                "float" java.lang.Float
                "double" java.lang.Double
                "string" java.lang.String
                "void" schema.core/Any})

(defn capitalize-name [name]
  (->> name
    (#(string/replace %1 #"([A-Z])" "_$1"));hack to keep existing camelCase
    (#(string/split %1 #"_"))
    (map string/capitalize)
    (apply str)))

(defn to-schema-fields
  "turns AST of message's fields into Schema field list
  Input has to be vector of fields AST as it shown here
    [
      [:FIELD
        [:FIELD_IDENT \"0\"]
        [:DATA_TYPE \"string\"]
        [:NAME_IDENT \"label\"]]]
  which is translated into:
    [label :- java.lang.String]"
  [field-items]
  (vec
    (reduce concat []
      (for [field field-items]
        (let [field-node (zip/vector-zip field)
              type-node  (-> field-node zip/down zip/right zip/right zip/node)
              field-type (second type-node)
              field-name (-> field-node zip/down zip/rightmost zip/node second)
              data-type (cond
                          (contains? datatypes field-type)
                            (get datatypes field-type)
                          (= :LIST (first field-type))
                            [(get datatypes (-> field-type second second))]
                          (= :MAP (first field-type))
                            {(get datatypes (-> field-type second second))
                             (get datatypes (-> field-type last second))}
                          :else (symbol (capitalize-name field-type)))]
          [(symbol field-name) :- data-type])))))

(defn to-message
  "turns message AST into Prismatic's annotated record
  It works for message AST and exception AST
  Usage:
    (def msg-ast (idl-parser.core/parse-one \"message Test {1: int key}\")
    (to-schema-record msg-ast)
  Returns:
    a generated Clojure code, which declares properly annotated record."
  [msg-ast]
  (let [msg-tree (zip/vector-zip msg-ast)
        msg-items (-> msg-tree zip/down zip/rights)
        name-node (first msg-items)
        field-items (rest msg-items)
        msg-name (-> name-node second capitalize-name)]
    `(schema/defrecord
       ~(symbol msg-name)
       ~(to-schema-fields field-items))))


(defn to-schema-function
  "translates function AST into source code of Clojure function, which
  is properly annotated with Pristmatic's Schema library
  Usage:
    (def function-ast
      [:FUNCTION
        [:DATA_TYPE \"int\"]
        [:NAME_IDENT \"train\"]
        [:FUNCTION_ARGS
          [:FIELD
          [:FIELD_IDENT \"0\"]
          [:DATA_TYPE [:LIST [:DATA_TYPE \"int\"]]]
          [:NAME_IDENT \"data\"]]]])
    (to-schema-function function-ast)
  Returns:
    a generated source code for the function AST"
  [function-ast]
  (let [function-tree (zip/vector-zip function-ast)
        return-type (-> function-tree zip/down zip/right zip/node last)
        function-name (-> function-tree zip/down zip/right zip/right zip/node last)
        function-args (-> function-tree zip/down zip/rightmost zip/node rest)]
    `(schema/defn
       ~(symbol function-name) :- ~(get datatypes return-type)
       ~(to-schema-fields function-args))))

;; it makes more sense to generate clojure protocols/types for services,
;; NB! it doesnt support versioning and inherits
(defn to-service
  "translates service AST into vector of source of annotated schema-function"
  [service-ast]
  (let [service-tree (zip/vector-zip service-ast)
        service-info (zip/vector-zip
                       (vec (-> service-tree zip/down zip/right zip/children)))
        service-items (-> service-tree zip/down zip/rights rest)]
    {:service (-> service-info zip/down zip/right zip/node second)
     :version (if-not (-> service-info zip/down zip/right zip/right)
                (-> service-info zip/down zip/rightmost zip/node second)
                "0")
     :functions (for [item-ast service-items
                      :when (= :FUNCTION (first item-ast))]
                  (to-schema-function item-ast))
     :inherits []}))

(defn to-enum
  "translates enum AST into plain Clojure vector.
  Usage:
    (def enum-ast
      [:ENUM_BLOCK
        [:NAME_IDENT \"T\"]
        [:ENUM_FIELD [:FIELD_IDENT \"0\"] [:NAME_IDENT \"RED\"]]
        [:ENUM_FIELD [:FIELD_IDENT \"1\"] [:NAME_IDENT \"YELLOW\"]]])
    (to-enum enum-ast)
  Returns:
    a Clojure vector binded to the defined name"
  [enum-ast]
  (let [enum-tree (zip/vector-zip enum-ast)
        enum-name (-> enum-tree zip/down zip/right zip/node second)
        enum-items (-> enum-tree zip/down zip/rights rest) ; skip name field
        enum-vals  (doall
                    (for [field enum-items
                        :when (= :ENUM_FIELD (first field))]
                      (-> field last second)))]
    `(def
        ~(symbol (capitalize-name (str enum-name "Enum")))
        ~@(vec enum-vals))))
