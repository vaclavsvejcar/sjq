package dev.svejcar.sjq.core

object Sanitizer {

  val SpecialChars: Set[String] = Set("-", "_", ".", ":", "$")

  val ScalaKeywords: Set[String] = Set(
    "abstract",
    "case",
    "catch",
    "class",
    "def",
    "do",
    "else",
    "extends",
    "false",
    "final",
    "finally",
    "for",
    "forSome",
    "if",
    "implicit",
    "import",
    "lazy",
    "match",
    "new",
    "null",
    "object",
    "override",
    "package",
    "private",
    "protected",
    "return",
    "sealed",
    "super",
    "this",
    "throw",
    "trait",
    "true",
    "try",
    "type",
    "val",
    "var",
    "while",
    "with",
    "yield"
  )

  def containsWhitespaces(input: String): Boolean = !input.matches("""\S+""")
  def isKeyword(input: String): Boolean           = ScalaKeywords.contains(input)
  def containsSpecialChar(input: String): Boolean = SpecialChars.exists(input.contains)

  def sanitize(input: String): String =
    if (isKeyword(input) || containsWhitespaces(input) || containsSpecialChar(input)) s"`$input`" else input

}
