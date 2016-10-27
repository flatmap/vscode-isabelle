package net.flatmap.vscode

import io.circe.generic.semiauto._

case class IsabelleConfig()

object IsabelleConfig {
  implicit def encoder = deriveEncoder[IsabelleConfig]
  implicit def decoder = deriveDecoder[IsabelleConfig]
}

case class Config(languageServerExample: Option[IsabelleConfig])

object Config {
  implicit def encoder = deriveEncoder[Config]
  implicit def decoder = deriveDecoder[Config]
}


