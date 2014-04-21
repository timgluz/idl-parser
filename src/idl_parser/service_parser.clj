(ns idl-parser.service-parser
  (:require [instaparse.core :as instaparse]
            [clojure.java.io :as io]))

(comment
  (def service-parser (io/resource "idl-service.bnf"))

  (require '[instaparse.core :as insta])
  (require '[clojure.java.io :as io])

  (def parser (insta/parser (io/resource "parsers/service.bnf")))

  (parser "service TEST {}")
  (parser "service TEST { }")

  ;;functions
  (parser "service TEST {void set()}")
  (parser "service TEST {void set(1: int key)}")
  (parser "service TEST {int get(1: int key)}")
  (parser "service TEST {void set(1: int key = 0)}")

  (parser "service TEST {void set(1: int key, 2: int value)}")
  (parser "service TEST {void set(1: int key) int get(1: int key)}")

  ;;inherits
  (parser "service TEST {inherit *}")
  (parser "service TEST {inherit get}")
  (parser "service TEST {inherit void set()}")
  (parser "service TEST {inherit get inherit set}")

  ;; mix of inherits and functions
  (parser "service TEST {void set() inherit get}")



  )
