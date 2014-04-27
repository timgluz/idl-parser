(ns idl-parser.core
  (:require [idl-parser.lexer :as lexer]
            [idl-parser.translator :as translator]
            [instaparse.core :as insta]
            [clojure.java.io :as io]))

(def grammar (slurp (io/resource "grammars/idl.ebnf")))
(def parsers {:service (insta/parser grammar :start :SERVICE_BLOCK)
              :message (insta/parser grammar :start :MESSAGE_BLOCK)
              :exception (insta/parser grammar :start :EXCEPTION_BLOCK)
              :enum (insta/parser grammar :start :ENUM_BLOCK)})

(def translators {:service translator/to-service
                  :message translator/to-message
                  :exceptions translator/to-message
                  :enum translator/to-enum})

(defn parse-block
  "parses a block of IDL into AST tree"
  [block-text]
  (let [block-type (keyword (re-find #"\w+" block-text))
        parser (get parsers block-type)
        trans (get translators block-type)]
    (println "Going to parse:  \n" block-text)
    (if-not (empty? (seq block-text))
      {block-type [((comp trans parser) block-text)]}
      [])))

(defn merge-map-seq [merge-fn map-seq]
  "joins a list of map into one map"
  (reduce
    (fn [acc item]
      (merge-with merge-fn acc item))
    {} map-seq))

(defn parse-text
  "parses a sequence of blocks "
  [text]
  (->> text
    lexer/split-by-blocks
    (map parse-block)
    (merge-map-seq concat)))
