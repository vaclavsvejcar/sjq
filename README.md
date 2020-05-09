<p align="center"><img src ="https://github.com/vaclavsvejcar/sjq/raw/master/doc/assets/logo.png" width="200" /></p>

You may know the [jq JSON command line processor][web:jq]. You may also know the [ammonite REPL][web:ammonite-repl]. Now imagine that you combine these two awesome tools into single one and you have the __sjq__.

__sjq__ is small tool written in [Scala][web:scala] that compiles your input _JSON_ into _Scala case classes_, so you can use __Scala expressions__ and collections API __to transform your data__, no need to remember any special syntax! And thanks to the embedded [ammonite REPL][web:ammonite-repl], you can use all that goodies such as __syntax highlighting__, __auto completion on TAB__ and much more! Manipulating _JSON_ data has never been easier :-)

> 🚧 __Work in Progress__ 🚧 - This software is under active development and hasn't even reached its initial public release. Please bear in mind that documentation is likely missing and API and/or command line interface can change unexpectedly. At least for now, below is small demo how you can use __sjq__ right now.

## 1. Example of Use
[![asciicast](https://asciinema.org/a/iWnwjGEQH7jZ3FtnSSnp7TRs7.svg)](https://asciinema.org/a/iWnwjGEQH7jZ3FtnSSnp7TRs7)

## 2. Table of Contents
<!-- TOC -->

- [1. Example of Use](#1-example-of-use)
- [2. Table of Contents](#2-table-of-contents)
- [3. Installation](#3-installation)
    - [3.1. From Source Code](#31-from-source-code)
- [4. Usage](#4-usage)
    - [4.1. Interactive (REPL) Mode](#41-interactive-repl-mode)
        - [4.1.1. Example](#411-example)
        - [4.1.2. Command Line Options](#412-command-line-options)
        - [4.1.3. Exposed variables](#413-exposed-variables)
    - [4.2. Non-interactive (CLI) Mode](#42-non-interactive-cli-mode)
        - [4.2.1. Example](#421-example)
        - [4.2.2. Command Line Options](#422-command-line-options)
- [5. How it Works?](#5-how-it-works)
- [6. Current Limitations](#6-current-limitations)

<!-- /TOC -->

## 3. Installation

> Pre-built _JAR_ files and binary distribution coming soon!

### 3.1. From Source Code
To build __sjq__ from source code, you need to install [sbt][web:sbt] first.

```sh
$ git clone https://github.com/vaclavsvejcar/sjq.git
$ cd sjq/
$ sbt assembly
```

Then grab the built _JAR_ file from `./target/scala-2.13/sjq-assembly-<VERSION>.jar` and you're ready to go!

## 4. Usage
__sjq__ offers two different modes, the __Interactive (REPL) Mode__ which uses the [ammonite REPL][web:ammonite-repl] for interactive work, and __Non-interactive (CLI) Mode__ which is useful when you need to call __sjq__ from another shell script, etc.

### 4.1. Interactive (REPL) Mode
Probably the biggest advantage over similar tools is the interactive mode, powered by the awesome [ammonite REPL][web:ammonite-repl]. In this mode, __sjq__ takes your input JSON, compiles it into Scala code and then exposes them through the _ammonite REPL_. Then you can work with your data as with regular Scala code, with all the goodies such as __syntax highlighting__ and __auto completion on TAB__:

#### 4.1.1. Example

```sh

$ java -jar sjq.jar repl -f path/to/file.json

--- Welcome to sjq REPL mode ---
Compiling type definitions from input JSON (this may take a while)...


[i] Variable 'root' holds Scala representation of parsed JSON
[i] Variable 'json' holds parsed JSON
[i] Variable 'ast' holds internal AST representation of data (for debugging purposes)
[i] Variable 'defs' holds generated Scala definitions (for debugging purposes)
[i] To serialize data back to JSON use '.asJson.spaces2'


Welcome to the Ammonite Repl 2.1.1 (Scala 2.13.2 Java 1.8.0_222)
@ root.users 
res0: Seq[root0.users] = List(users("John Smith", 42.0), users("Peter Taylor", 67.0), users("Lucy Snow", 21.0))

@ root.users.sortBy(_.age) 
res1: Seq[root0.users] = List(users("Lucy Snow", 21.0), users("John Smith", 42.0), users("Peter Taylor", 67.0))
```

#### 4.1.2. Command Line Options
Interactive mode is executed using the `java -jar sjq.jar repl` and you need to specify the source of input JSON either as local file (`-f|--file=PATH`) or as inline value (`-j|--json=JSON`).

#### 4.1.3. Exposed variables
Following variables are exposed to the _ammonite REPL_ so you can access them as needed:

- `root` - _Scala_ representation of _JSON_ data, this is probably what you'll use the most
- `json` - [Circe's][web:circe] representation of parsed _JSON_ data
- `ast` - __sjq__'s internal representation of parsed _JSON_ data
- `defs` - _Scala_ definitions and types generated from the _JSON_ data

### 4.2. Non-interactive (CLI) Mode
If you don't want to use the interactive mode, then this mode is here for you. It might be useful for example for some _shell script_, when it can perform JSON transformations as part of some larger task.

#### 4.2.1. Example
```sh
$ cat /tmp/example.json | java -jar sjq.jar cli -a "root.users.sortBy(_.age).asJson.noSpaces"                                                                    
"[{\"name\":\"Lucy Snow\",\"age\":21.0},{\"name\":\"John Smith\",\"age\":42.0},{\"name\":\"Peter Taylor\",\"age\":67.0}]"
```

#### 4.2.2. Command Line Options
Non-interactive mode is executed using the `java -jar sjq.jar cli`. You need to specify the transformation code as `-a|--access=CODE` argument and input JSON as either _STDIN_ or as `-j|--json=JSON` argument.


## 5. How it Works?
For the curious ones, here's how __sjq__ works under the hood:

1. First step is to parse internal _AST_ representation from the input _JSON_ (see [dev.svejcar.sjq.core.Parser][meta:file/Parser]).
1. Next step is to emit valid _Scala_ code (_case classes_ and _objects_) matching the input _JSON_ (see [dev.svejcar.sjq.core.Emitter][meta:file/Emitter]). This code is the compiled in runtime (this is the part that may take long time).
1. Last step is to read the input JSON data into generated _Scala_ representation. This is done using the [Circe's automatic derivation][web:circe/auto-derivation] mechanism.

## 6. Current Limitations
__sjq__ is under heavy development and things might not be still perfect yet. Here is the list of known issues and/or limitations that should be targeted in future releases:

- __Performance:__ compile more complex __JSON__ can be __very slow__, as it needs to do both __compilation in runtime__ of generated _Scala_ code and __generic derivation__ for _Circe's decoders/encoders_. Performance improvements should be one of main targets in future releases
- __Occasional crashes:__ more complex __JSON__ structures might cause runtime crashes, mostly due to incorrectly generated _Scala_ code and/or not matching derived _JSON_ encoders/decoders. If that happens for you, please [report that as new issue][meta:issues].


[meta:file/Emitter]: https://github.com/vaclavsvejcar/sjq/blob/master/src/main/scala/dev/svejcar/sjq/core/Emitter.scala
[meta:file/Parser]: https://github.com/vaclavsvejcar/sjq/blob/master/src/main/scala/dev/svejcar/sjq/core/Parser.scala
[meta:file/Sanitizer]: https://github.com/vaclavsvejcar/sjq/blob/master/src/main/scala/dev/svejcar/sjq/core/Sanitizer.scala
[meta:issues]: https://github.com/vaclavsvejcar/sjq/issues
[web:ammonite-repl]: https://ammonite.io/#Ammonite-REPL
[web:circe]: https://circe.github.io/
[web:circe/auto-derivation]: https://circe.github.io/circe/codecs/auto-derivation.html
[web:jq]: https://stedolan.github.io/jq/
[web:sbt]: https://www.scala-sbt.org/
[web:scala]: https://www.scala-lang.org
