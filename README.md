# idl-parser

[![Build Status](https://travis-ci.org/timgluz/idl-parser.svg)](https://travis-ci.org/timgluz/idl-parser)

**IDL-parser** is experimental project, which parses Jubatus (or Thrift's) IDL and turns them
proper annotated [Pristmatic/Schema] dataobjects or function-skeleton.

I created this project to learn more about Instaparse library and it's functionalities.


## Usage

```
  (require '[instaparse.core :as insta])
  (require '[clojure.java.io :as io])

  (def grammar (slurp (io/resource "grammars/idl.ebnf")))
  (def service-parser (insta/parse grammar :SERVICE_BLOCK))

  (service-parser "service TEST {int set(1: int key)}")
  
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
