(ns idl-parser.core
  (:require [idl-parser.lexer]
            [idl-parser.parser]
            [insta]))

(def grammar (slurp (io/resource "grammars/idl.ebnf")))
(def parsers {:service (insta/parser grammar :start :SERVICE_BLOCK)
              :message (insta/parser grammar :start :MESSAGE_BLOCK)
              :exception (insta/parser grammar :start :EXCEPTION_BLOCK)
              :enum (insta/parser grammar :start :ENUM_BLOCK)})

(defn parse-one
  "parses a block of IDL into AST tree"
  [text]
  (let [block-type (keyword (re-find #"\w+" text))
        parser (get parsers block-type)]
    (parser text)))

(defn parse-all
  "parses sequence of blocks"
  [block-seq]
  (map parse-one block-seq))
