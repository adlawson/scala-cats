package com.adlawson.cats.data

import cats.ApplicativeError
import cats.data.{Kleisli => kleisli, Xor, XorT}

object Kleisli extends KleisliOps with KleisliInstances

trait KleisliInstances {
  implicit def toKleisliError[F[_], A, B, E](k: Kleisli[F, A, B]): KleisliError[F, A, B, E] =
    KleisliError(k)
}

private[data] sealed trait KleisliOps {
  def apply[F[_], A, B](f: A => F[B]): Kleisli[F, A, B] =
    kleisli(f)
}

private[data] final case class KleisliError[F[_], A, B, E](k: Kleisli[F, A, B]) {
  type XorF[T] = XorT[F, E, T]

  def attempt(implicit F: ApplicativeError[F, E]): Kleisli[F, A, E Xor B] =
    kleisli(a => F.handleErrorWith(F.map(k.run(a))(Xor.right[E, B]))(e => F.pure(Xor.left(e))))

  def attemptT(implicit F: ApplicativeError[F, E]): Kleisli[XorF, A, B] =
    kleisli(a => XorT(attempt.run(a)) : XorF[B])

  def handleError(f: E => B)(implicit F: ApplicativeError[F, E]): Kleisli[F, A, B] =
    recover { case t => f(t) }

  def handleErrorWith(f: E => Kleisli[F, A, B])(implicit F: ApplicativeError[F, E]): Kleisli[F, A, B] =
    recoverWith { case t => f(t) }

  def handleErrorWithF(f: E => F[B])(implicit F: ApplicativeError[F, E]): Kleisli[F, A, B] =
    recoverWithF { case t => f(t) }

  def recover(pf: PartialFunction[E, B])(implicit F: ApplicativeError[F, E]): Kleisli[F, A, B] =
    kleisli(a => F.recover(k.run(a))(pf))

  def recoverWith(pf: PartialFunction[E, Kleisli[F, A, B]])(implicit F: ApplicativeError[F, E]): Kleisli[F, A, B] =
    kleisli(a => F.recoverWith(k.run(a))(pf.andThen(_.run(a))))

  def recoverWithF(pf: PartialFunction[E, F[B]])(implicit F: ApplicativeError[F, E]): Kleisli[F, A, B] =
    kleisli(a => F.recoverWith(k.run(a))(pf))
}
