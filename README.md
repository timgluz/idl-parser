# idl-parser

[![Build Status](https://travis-ci.org/timgluz/idl-parser.svg)](https://travis-ci.org/timgluz/idl-parser)

**IDL-parser** is a experimental project, which purpose was solely to learn the power of [Instaparse](https://github.com/Engelberg/instaparse) and usage of Prismatic's [Schema](https://github.com/Prismatic/schema). So i'm not sure how helpful its for you; but i'm quite proud of building fully tested IDL grammar and translators as my sideproject; 

It takes text of Jubatus (or Thrift's) IDL (like this [here](https://github.com/jubatus/jubatus/blob/master/jubatus/server/server/classifier.idl) ) and turns them into proper annotated [Pristmatic/Schema] code.


## Usage

#### Using a specific parser

```
  (require '[instaparse.core :as insta])
  (require '[clojure.java.io :as io])

  (def grammar (slurp (io/resource "grammars/idl.ebnf")))
  (def service-parser (insta/parse grammar :SERVICE_BLOCK))

  (service-parser "service TEST {int set(1: int key)}")
  
```

#### Parsing file into Pristmatic/Schema code


```
	(require '[clojure.java.io :as io])
	(require '[idl-parser.core :as idl-parser])
	
	(def content (slurp (io/resource "examples/classifier.idl")))
	(idl-parser/parse-text content)
	
;;=> responses with
	
{:service
 [{:service "classifier",
   :version "classifier",
   :functions
   ((schema.core/defn
     train
     :-
     java.lang.Integer
     [data :- [LabeledDatum]])
    (schema.core/defn classify :- nil [data :- [Datum]])
    (schema.core/defn get_labels :- nil [])
    (schema.core/defn
     set_label
     :-
     java.lang.Boolean
     [new_label :- java.lang.String])
    (schema.core/defn clear :- java.lang.Boolean [])),
   :inherits []}],
 :message
 ((schema.core/defrecord
   EstimateResult
   [label :- java.lang.String score :- java.lang.Double])
  (schema.core/defrecord
   LabeledDatum
   [label :- java.lang.String data :- Datum]))}
```



## Notes

It's not production ready - it doesnt handle versioning and other fancy features of Thrift's IDL. 

## License

Copyright © 2014 Tauho

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
