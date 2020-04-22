<p align="center"><img src ="https://github.com/vaclavsvejcar/sjq/raw/master/doc/assets/logo.png" width="200" /></p>

You may know the [jq JSON command line processor][web:jq]. You may also know the [ammonite REPL][web:ammonite-repl]. Now imagine that you combine these two awesome tools into single one and you have the __sjq__.

__sjq__ is small tool written in [Scala][web:scala] that compiles your input _JSON_ into _Scala case classes_, so you can use __Scala expressions__ and commands __to manipulate your data__ and then export them again as _JSON_ (if needed). And thanks to the embedded [ammonite REPL][web:ammonite-repl], you can use all that goodies such as __syntax highlighting__, __auto completion on TAB__ and much more! Manipulating _JSON_ data has never been easier :-)

> 🚧 __Work in Progress__ 🚧 - This software is under active development and hasn't even reached its initial public release. Please bear in mind that documentation is likely missing and API and/or command line interface can change unexpectedly. At least for now, below is small demo how you can use __sjq__ right now.

## Example of Use

```sh
$ cat ./example.json
{
    "fruit": "Apple",
    "size": "Large",
    "color": "Red"
}

$ java -jar ./sjq.jar repl -j "$(cat ./example.json)"

--- Welcome to sjq REPL mode ---
Compiling type definitions from input JSON (this may take a while)...


[i] To access the data parsed from JSON use the '_root' variable.
[i] To serialize data back to JSON use '.asJson.spaces2'


Welcome to the Ammonite Repl 2.0.4 (Scala 2.13.1 Java 1.8.0_222)
@ _root 
res0: root = root("Apple", "Large", "Red")

@ _root.size 
res1: String = "Large"

@ _root.copy(size = "Small").asJson.spaces2 
res3: String =
"""{
  "fruit" : "Apple",
  "size" : "Small",
  "color" : "Red"
}"""


```


[web:ammonite-repl]: https://ammonite.io/#Ammonite-REPL
[web:jq]: https://stedolan.github.io/jq/
[web:scala]: https://www.scala-lang.org
