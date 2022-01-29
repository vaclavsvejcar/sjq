package dev.svejcar.sjq.service

import zio.*

trait Sanitizer:
  def sanitize(input: String): UIO[String]

object Sanitizer:
  val live: ULayer[SanitizerLive]                      = ZLayer.succeed(SanitizerLive())
  def sanitize(input: String): URIO[Sanitizer, String] = ZIO.serviceWithZIO(_.sanitize(input))

case class SanitizerLive() extends Sanitizer:

  val SpecialChars: Set[String] = Set("-", "_", ".", ":", "$")
  val ScalaKeywords: Set[String] = Set(
    "abstract",
    "case",
    "catch",
    "class",
    "def",
    "do",
    "else",
    "enum",
    "export",
    "extends",
    "false",
    "final",
    "finally",
    "for",
    "given",
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
    "then",
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

  override def sanitize(input: String): UIO[String] =
    ZIO.succeed(
      if (isKeyword(input) || containsWhitespaces(input) || containsSpecialChar(input)) s"`$input`" else input
    )

  def containsWhitespaces(input: String): Boolean = !input.matches("""\S+""")
  def isKeyword(input: String): Boolean           = ScalaKeywords.contains(input)
  def containsSpecialChar(input: String): Boolean = SpecialChars.exists(input.contains)
