import mill._, scalalib._

object `smart-guilds` extends ScalaModule {
  def scalaVersion = "2.13.1"

  override def ivyDeps = Agg(
    ivy"org.typelevel::cats-core:2.1.1",
    ivy"co.fs2::fs2-core:2.4.0",
    ivy"org.scalacheck::scalacheck:1.14.1"
  )

}