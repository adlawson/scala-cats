package com.adlawson.cats.data

import cats.ApplicativeError
import cats.std.FutureInstances
import cats.scalatest.XorValues
import org.scalatest.{EitherValues, FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.{ExecutionContext, Future}

class KleisliSpec extends FlatSpec with Matchers {

  "Kleisli[Future, A, B]" should behave like new KleisliInstances with FutureInstances with ScalaFutures {

    sealed trait Fixtures {
      implicit val ec = ExecutionContext.global
      lazy val exception = new RuntimeException("KleisliSpec exception")
    }

    "apply" should "return a new Kleisli[Future, A, B] that successfully resolves" in new Fixtures {
      val k = Kleisli[Future, Int, String](i => Future.successful(i.toString))
      whenReady(k.run(1234))(_ shouldBe "1234")
    }

    "attempt" should "return a new Kleisli[Future, A, E Xor B] that successfully resolves to the Right" in new Fixtures with XorValues {
      val k = Kleisli[Future, Int, String](i => Future.successful(i.toString))
      whenReady(k.attempt.run(1234))(_.value shouldBe "1234")
    }

    it should "return a new Kleisli[Future, A, E Xor B] that successfully resolves to the Left" in new Fixtures with XorValues {
      val k = Kleisli[Future, Int, String](_ => Future.failed(exception))
      whenReady(k.attempt.run(1234))(_.leftValue shouldBe exception)
    }

    "attemptT" should "return a new Kleisli[Kleisli#XorF, A, B] that successfully resolves to the Right" in new Fixtures with XorValues {
      val k = Kleisli[Future, Int, String](i => Future.successful(i.toString))
      whenReady(k.attemptT.run(1234).value)(_.value shouldBe "1234")
    }

    it should "return a new Kleisli[Kleisli#XorF, A, B] that successfully resolves to the Left" in new Fixtures with XorValues {
      val k = Kleisli[Future, Int, String](_ => Future.failed(exception))
      whenReady(k.attemptT.run(1234).value)(_.leftValue shouldBe exception)
    }

    "handleError" should "return a new Kleisli[Future, A, B] that successfully resolves after handling an error" in new Fixtures {
      val k = Kleisli[Future, Int, String](i => Future.failed(exception))
      def f(t: Throwable) = t.getMessage
      whenReady(k.handleError(f).run(1234))(_ shouldBe exception.getMessage)
    }

    "handleErrorWith" should "return a new Kleisli[Future, A, B] that successfully resolves after handling an error" in new Fixtures {
      val k = Kleisli[Future, Int, String](i => Future.failed(exception))
      def f(t: Throwable) = Kleisli[Future, Int, String](i => Future.successful(i.toString))
      whenReady(k.handleErrorWith(f).run(1234))(_ shouldBe "1234")
    }

    "handleErrorWithF" should "return a new Kleisli[Future, A, B] that successfully resolves after handling an error" in new Fixtures {
      val k = Kleisli[Future, Int, String](i => Future.failed(exception))
      def f(t: Throwable) = Future.successful(t.getMessage)
      whenReady(k.handleErrorWithF(f).run(1234))(_ shouldBe exception.getMessage)
    }

    "recover" should "return a new Kleisli[Future, A, B] that successfully resolves after recovering from an error" in new Fixtures {
      val k = Kleisli[Future, Int, String](i => Future.failed(exception))
      val f: PartialFunction[Throwable, String] = { case t => t.getMessage }
      whenReady(k.recover(f).run(1234))(_ shouldBe exception.getMessage)
    }

    "recoverWith" should "return a new Kleisli[Future, A, B] that successfully resolves after recovering from an error" in new Fixtures {
      val k = Kleisli[Future, Int, String](i => Future.failed(exception))
      val f: PartialFunction[Throwable, Kleisli[Future, Int, String]] = { case t => Kleisli(i => Future.successful(i.toString)) }
      whenReady(k.recoverWith(f).run(1234))(_ shouldBe "1234")
    }

    "recoverWithF" should "return a new Kleisli[Future, A, B] that successfully resolves after recovering from an error" in new Fixtures {
      val k = Kleisli[Future, Int, String](i => Future.failed(exception))
      val f: PartialFunction[Throwable, Future[String]] = { case t => Future.successful(t.getMessage) }
      whenReady(k.recoverWithF(f).run(1234))(_ shouldBe exception.getMessage)
    }
  }
}
