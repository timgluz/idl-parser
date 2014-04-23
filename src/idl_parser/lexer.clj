;; Namespace which includes functions to prepare data for parsers

(ns idl-parser.lexer
  (:require [clojure.string :as string])
  (:import [clojure.lang PersistentQueue]))

(def matching-pairs {\{ \}
                     \( \)
                     \[ \]})

(defn is-starting-pair [char1]
  (contains? matching-pairs char1))

(defn is-matching-pair [char1 char2]
  (and (contains? matching-pairs char1)
       (= char2 (get matching-pairs char1))))

(defn warn-unmatched-brackets [& text-items]
  (println (apply str text-items)))

(defn match-brackets
  "find a locations of matching pairs.
  It's uses a classical balanced parenthesis algorithm:
    1. use a character stack to keep a opening brackets
    2. traverse the expression string
      2.1 if the current character is a starting bracket,
          then push it into the stack and take a next char

      2.2 if the current character is a closing bracket,
        a) pop the last opening bracket from the stack
        b) if bracket's are matching, add it into results
        **NB!** next step is a modification of the original
                algorithm, just to fit it for it's task
        c) otherwise ignore it
    3. after complete traversal, if there is some starting
      bracket left in the stack, then it's not balanced
      But it'll ignore it and will just log warning message
  source:
    http://www.geeksforgeeks.org/check-for-balanced-parentheses-in-an-expression/
  Usage:
    (match-brackets \"def text(){ fun(1,2){print 1,2,3}}\")"
  [text]
  (let [results (atom [])]
    (loop [char-seq (seq text) stack [], current-pos 0]
      (if (empty? char-seq)
        ;; return the result and finish iteration
        (do
          (when-not (empty? stack)
            (warn-unmatched-brackets
              "Unmatched brackets for: " stack))
          @results)
        ;; match parenthesis until there's characters
        (let [current-char (first char-seq)
              [starting-char, start-pos] (peek stack)
               stack (cond
                        ;; if it's a beginning of block - add it into stack
                        (is-starting-pair current-char)
                          (conj stack [current-char current-pos])
                        ;; if it's a closing bracket; add it into results
                        ;; and remove a starting bracket from stack
                        (is-matching-pair starting-char current-char)
                          (do
                            (reset! results
                                    (conj @results
                                          [starting-char start-pos
                                            current-char current-pos]))
                            (pop stack))
                        ;; keep it as it is
                        :else stack)]
          (recur (rest char-seq) stack (inc current-pos)))))))

(defn remove-comments [text]
  (string/replace text #"\#.*" ""))

(defn remove-newlines [text]
  (string/replace text #"\n|\r\n" " "))

(defn replace-whitespace [text]
  (string/replace text #"\s+" " "))

(defn clean-text
  "remove all comments, newline characters
  and repeating multiple whitespaces"
  [text]
  (-> text remove-comments remove-newlines replace-whitespace string/trim))

(defn split-by-blocks
  "splits IDL text into <name> {IDL-notation} block sequence,
  which are parseble by the defined IDL grammar;"
  [text]
  (let [get-sorted-positions (fn [text]
                               (sort-by second (match-brackets text)))]
    (loop [cur-text (clean-text text), results []]
      (if (empty? (seq cur-text))
        results
        (let [positions (get-sorted-positions cur-text)
              [_, start, _ end] (first positions)]
          (recur (subs cur-text (inc end))
                 (conj results
                       (string/trim (subs cur-text 0 (inc end))))))))))

