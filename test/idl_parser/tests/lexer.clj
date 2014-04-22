(ns idl-parser.tests.lexer
  (:require [midje.sweet :refer :all]
            [idl-parser.lexer :as lexer]))

(facts "is-starting-pair"
  (fact "returns correct results"
    (lexer/is-starting-pair \() => true
    (lexer/is-starting-pair \[) => true
    (lexer/is-starting-pair \{) => true
    (lexer/is-starting-pair \}) => false
    (lexer/is-starting-pair \)) => false
    (lexer/is-starting-pair \a) => false
    (lexer/is-starting-pair \#) => false))

(facts "is-matching-pair"
  (fact "returns correct results"
    (lexer/is-matching-pair \( \)) => true
    (lexer/is-matching-pair \{ \}) => true
    (lexer/is-matching-pair \[ \]) => true
    (lexer/is-matching-pair \( \}) => false
    (lexer/is-matching-pair \{ \)) => false
    (lexer/is-matching-pair \a \b) => false
    (lexer/is-matching-pair \( \A) => false))

(facts "match-brackets"
  (fact "works with with bare examples"
    (lexer/match-brackets "()") => [[\( 0 \) 1]]
    (lexer/match-brackets "[]") => [[\[ 0 \] 1]]
    (lexer/match-brackets "{}") => [[\{ 0 \} 1]]
    (lexer/match-brackets "()()") => [[\( 0 \) 1] [\( 2 \) 3]])
  (fact "works with mixed characters"
    (lexer/match-brackets "(a)") => [[\( 0 \) 2]]
    (lexer/match-brackets "func(){return 1;}") =>
      [[\( 4 \) 5] [\{ 6 \} 16]])
  (fact "works with mismatched brackets"
    (lexer/match-brackets "(a)(") => [[\( 0 \) 2]]
    (lexer/match-brackets "func(){") => [[\( 4 \) 5]]))

(facts "cleanups texts"
  (fact "removes full-line comments"
    (lexer/remove-comments "# readme: comment\nABC") => "\nABC"
    (lexer/remove-comments "abc\n#fix: me\ndef") => "abc\n\ndef"
    (lexer/remove-comments "#-- bla bla
      # bla
      service
      # bla-bla
      msg
      # bla-bla")
      "\n      \n      service\n      \n      msg\n")
  (fact "removes multiline characters"
    (lexer/remove-newlines "abc\ndef") => "abc def"
    (lexer/remove-newlines "ab\ncd\nef") => "ab cd ef"))

(facts "split-by-blocks"
  (fact "splits simple examples into blocks"
    (lexer/split-by-blocks "") => []
    (lexer/split-by-blocks "service {}") => ["service {}"]
    (lexer/split-by-blocks "#--comment--\nservice {}")
        => ["service {}"])
  (facts "splits into multiple blocks"
    (lexer/split-by-blocks
      "service {func(1: int get)} enum {1: RED}") =>
        ["service {func(1: int get)}" "enum {1: RED}"]
    (lexer/split-by-blocks
      "# -- it's just dummy demo
      service T1 {func(1: int key)}
      service T2 {func(2: int key)}
      # -- here some message for service
      message M1 {1: int val}") =>
      ["service T1 {func(1: int key)}"
       "service T2 {func(2: int key)}"
       "message M1 {1: int val}"]))
