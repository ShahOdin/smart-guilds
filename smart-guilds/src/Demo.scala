import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import cats.syntax.option._
import org.scalacheck.Gen
import org.scalacheck.Gen.Parameters.default
import org.scalacheck.rng.Seed

object Demo extends App {

  sealed trait Contractor extends Product
  object Contractor {
    case object Alice extends Contractor
    case object Bob extends Contractor
    case object Charlie extends Contractor

    val all = List(Alice, Bob, Charlie)

    //assumptions: static
    implicit val likelyPerformance: Map[Contractor, Gen[Rep]] = Map(
      Alice -> Gen.chooseNum(-10, 20).map(Rep.apply),
      Bob -> Gen.chooseNum(-15, 15).map(Rep.apply),
      Charlie -> Gen.chooseNum(-20, 10).map(Rep.apply),
    )
  }

  case class DailyRate(value: Double)
  case class Rep(value: Int)
  case class Balance(value: Double)

  trait Cooperation {
    def hireContractor(dailyRate: DailyRate): Option[Contractor]
    def retireContractor(contractor: Contractor): Unit

    def incrementRating(contractor: Contractor): Unit
    def decrementRating(contractor: Contractor): Unit
    def payForService(contractor: Contractor, dailyRate: DailyRate): Unit
    def submitPaperWork(): Unit
  }

  object Cooperation {

    implicit class CooperationOps(coop: Cooperation) {

      private def submitReview(rep: Rep, contractor: Contractor) =
        rep.value match {
          case i if i > 0 =>
            for (_ <- Range(1, i + 1)) coop.incrementRating(contractor)
          case i =>
            for (_ <- Range(1, -i + 1)) coop.decrementRating(contractor)
        }

      def recruitForProject(
        budget: DailyRate
      )(implicit likelyPerformance: Map[Contractor, Gen[Rep]]): Option[Unit] =
        for {
          contractor <- coop.hireContractor(budget)
          rep = likelyPerformance(contractor).pureApply(default, Seed.random)
          _ = submitReview(rep, contractor)
          _ = coop.payForService(contractor, budget)
          _ = coop.retireContractor(contractor)
        } yield ()
    }

    trait AdminWork extends Cooperation {

      protected val availableForHire: ListBuffer[Contractor] =
        ListBuffer.from(Contractor.all)
      import Demo.Contractor.{Alice, Bob, Charlie}
      protected val reps: mutable.Map[Contractor, Rep] =
        mutable.Map(Alice -> Rep(30), Bob -> Rep(30), Charlie -> Rep(30))

      protected val balances: mutable.Map[Contractor, Balance] = mutable.Map(
        Alice -> Balance(0),
        Bob -> Balance(0),
        Charlie -> Balance(0)
      )

      override def incrementRating(contractor: Contractor): Unit =
        reps.updateWith(contractor)(_.map(r => Rep(r.value + 1)))

      override def decrementRating(contractor: Contractor): Unit =
        reps.updateWith(contractor)(_.map(r => Rep(r.value - 1)))

      //poc. should reps be normalised?
      private def rep(dailyRate: DailyRate): Option[Rep] =
        dailyRate.value match {
          case i if i < 0   => none
          case i if i < 100 => Rep(10).some
          case i if i < 200 => Rep(20).some
          case _            => Rep(30).some
        }

      private def chooseAcceptableRep(dailyRate: DailyRate,
                                      reps: List[Rep]): Rep =
        reps
          .find(r => rep(dailyRate).exists(_.value <= r.value))
          .getOrElse(reps.maxBy(_.value))

      override def hireContractor(dailyRate: DailyRate): Option[Contractor] = {
        val availableReps = availableForHire.map(reps.apply)
        val acceptableRep: Rep =
          chooseAcceptableRep(dailyRate, availableReps.toList)

        val potentialHire: Option[Contractor] =
          availableForHire.find(c => acceptableRep == reps(c))

        potentialHire.map { c =>
          availableForHire -= c; c
        }
      }

      override def retireContractor(contractor: Contractor): Unit =
        availableForHire += contractor

      override def submitPaperWork(): Unit = {
        balances.foreach(println)
        reps.foreach(println)
      }
    }

    def minimal: Cooperation = new Cooperation with AdminWork {
      override def payForService(contractor: Contractor,
                                 dailyRate: DailyRate): Unit =
        balances.updateWith(contractor)(
          _.map(b => Balance(b.value + dailyRate.value))
        )
    }

    def ubiPercentage: Cooperation = new Cooperation with AdminWork {
      override def payForService(contractor: Contractor,
                                 dailyRate: DailyRate): Unit = {
        val tax = Balance(dailyRate.value / 5)
        val remaining = Balance(dailyRate.value - tax.value)

        Contractor.all.foreach(
          c =>
            balances.updateWith(c)(
              _.map(b => Balance(b.value + tax.value / balances.size))
          )
        )

        balances.updateWith(contractor)(
          _.map(b => Balance(b.value + remaining.value))
        )
      }
    }

    def ubiFixed: Cooperation = new Cooperation with AdminWork {
      override def payForService(contractor: Contractor,
                                 dailyRate: DailyRate): Unit = {

        val tax = if (dailyRate.value > 100) Balance(100) else Balance(0)

        val remaining = Balance(dailyRate.value - tax.value)

        Contractor.all.foreach(
          c =>
            balances.updateWith(c)(
              _.map(b => Balance(b.value + tax.value / balances.size))
          )
        )

        balances.updateWith(contractor)(
          _.map(b => Balance(b.value + remaining.value))
        )
      }
    }

    def communism: Cooperation = new Cooperation with AdminWork {
      override def payForService(contractor: Contractor,
                                 dailyRate: DailyRate): Unit =
        Contractor.all.foreach(
          c =>
            balances.updateWith(c)(
              _.map(b => Balance(b.value + dailyRate.value / balances.size))
          )
        )
    }

  }

  import Contractor._
  val coop = Cooperation.ubiPercentage

  coop.recruitForProject(DailyRate(400))
  coop.recruitForProject(DailyRate(500))
  coop.recruitForProject(DailyRate(300))
  coop.recruitForProject(DailyRate(400))
  coop.recruitForProject(DailyRate(500))
  coop.recruitForProject(DailyRate(300))

  coop.submitPaperWork()
}
