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
                "string" java.lang.String})

(defn capitalize-name [name]
  (apply str
         (map string/capitalize
              (string/split name #"_"))))

(defn translate-message-fields
  "turns AST of message's fields into Schema field list
  Input has to be vector of fields AST as it shown here
    [[:FIELD [:FIELD_IDENT \"0\"] [:DATA_TYPE \"string\"] [:NAME_IDENT \"label\"]]]
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

(defn to-schema-record [msg-ast]
  "turns message AST into Prismatic's annotated record
  Usage:
    (def msg-ast (idl-parser.core/parse-one \"message Test {1: int key}\")
    (to-schema-record msg-ast)
  Returns:
    a generated Clojure code, which declares properly annotated record."
  (let [msg-tree (zip/vector-zip msg-ast)
        msg-items (-> msg-tree zip/down zip/rights)
        name-node (first msg-items)
        field-items (rest msg-items)
        msg-name (-> name-node second capitalize-name)]
    `(schema/defrecord
       ~(symbol msg-name)
       ~(translate-message-fields field-items))))

