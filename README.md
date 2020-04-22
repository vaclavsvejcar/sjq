<p align="center"><img src ="https://github.com/vaclavsvejcar/sjq/raw/master/doc/assets/logo.png" width="200" /></p>

You may know the [jq JSON command line processor][web:jq]. You may also know the [ammonite REPL][web:ammonite-repl]. Now imagine that you combine these two awesome tools into single one and you have the __sjq__.

__sjq__ is small tool written in [Scala][web:scala] that compiles your input _JSON_ into _Scala case classes_, so you can use __Scala expressions__ and commands __to manipulate your data__ and then export them again as _JSON_ (if needed). And thanks to the embedded [ammonite REPL][web:ammonite-repl], you can use all that goodies such as __syntax highlighting__, __auto completion on TAB__ and much more! Manipulating _JSON_ data has never been easier :-)

> 🚧 __Work in Progress__ 🚧 - This software is under active development and hasn't even reached its initial public release. Please bear in mind that documentation is likely missing and API and/or command line interface can change unexpectedly. At least for now, below is small demo how you can use __sjq__ right now.

## 1. Example of Use
<a href="https://asciinema.org/a/X8V6QCX6VDfZHU7CsCWIO9uet" target="_blank"><img src="https://asciinema.org/a/X8V6QCX6VDfZHU7CsCWIO9uet.svg" /></a>


## 2. Table of Contents
<!-- TOC -->

- [1. Example of Use](#1-example-of-use)
- [2. Table of Contents](#2-table-of-contents)
- [3. Installation](#3-installation)
    - [3.1. From Source Code](#31-from-source-code)
- [4. Usage](#4-usage)
    - [4.1. Interactive (REPL) Mode](#41-interactive-repl-mode)
    - [4.2. Non-interactive (CLI) Mode](#42-non-interactive-cli-mode)
- [5. How it Works?](#5-how-it-works)

<!-- /TOC -->

## 3. Installation

> At this moment, there's no binary distribution of sjq. Stay tuned, it's planned to be added soon :-)

### 3.1. From Source Code
In order to build __sjq__ from source code, you need to have installed the [sbt][web:sbt].

```sh
$ git clone https://github.com/vaclavsvejcar/sjq.git
$ cd sjq/
$ sbt assembly
```

Then grab the built _JAR_ file from `./target/scala-2.13/sjq-assembly-<VERSION>.jar` and you're ready to go!

## 4. Usage
__sjq__ offers two different modes, the __Interactive (REPL) Mode__ which uses the _ammonite REPL_ for interactive work, and __Non-interactive (CLI) Mode__ which is useful when you need to call __sjq__ from another shell script, etc.

### 4.1. Interactive (REPL) Mode
todo

### 4.2. Non-interactive (CLI) Mode
todo

## 5. How it Works?
todo


[web:ammonite-repl]: https://ammonite.io/#Ammonite-REPL
[web:jq]: https://stedolan.github.io/jq/
[web:sbt]: https://www.scala-sbt.org/
[web:scala]: https://www.scala-lang.org
