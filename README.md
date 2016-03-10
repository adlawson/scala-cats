# Cats extended

<img src="https://twistedsifter.files.wordpress.com/2014/05/longcat-hug-gif-remix-reddit-funny-6.gif?w=220&h=280" align="right"/>

[![Master branch build status][ico-build]][travis]

This library adds useful extensions to the [typelevel/cats][cats] library.
So far it implements:
 - MonadError to Kleisli instances (useful for working with Futures)

## Using Kleisli to define application flow
Here's an example of a printer service that connects to a printer over USB (could be anything, really).
There are many combinations of commands you can send, each needing a connection to be available.
Try to not focus on the specifics of printing and take note of things like:
 - Where dependencies are injected
 - Support for nested flows
 - Failure recovery

This has absolutely nothing whatsoever to do with any project I've worked on commercially. I Promise. Okay maybe a bit. Don't tell.

```scala
import com.adlawson.cats.implicits._ // Allows Kleisli#recoverWith to work
import cats.data.Kleisli
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait Broker {
  def publish(msg: String): Future[Unit]
  def request[A](msg: String): Future[A]
}
class UsbConnection extends Broker {
  def publish(msg: String) = ???
  def request(msg: String) = ???
}

object Printer {

  type Task[A] = Kleisli[Future, Broker, A]

  def task[A](f: Broker => Future[A])(implicit ec: ExecutionContext): Task[A] = Kleisli(f)

  def getValue(key: String)(implicit ec: ExecutionContext): Task[Int] =
    task(_.request[Int])(s"value command here with $key")

  def loadPage()(implicit ec: ExecutionContext): Task[Unit] =
    task(_.publish("load command here"))

  def printDocument(doc: java.io.InputStream)(implicit ec: ExecutionContext): Task[Unit] =
    task(_.publish(s"print command here with $doc"))

  def authenticate()(implicit ec: ExecutionContext): Task[Unit] = for {
    foo <- getValue("foo")
    bar <- getValue("bar")
    baz =  foo + bar
    bam <- task(_.publish(s"send rubbish authentication code with $bam"))
  } yield bam
}

/**
 * Define the application flow
 * Here we flatmap a bunch of commands together. Some of them take arguments and
 * and we're also able to recover from failures. What's interesting to see
 * though is that we still haven't got an open USB connection. That comes later.
 */
val task = for {
  auth <- Printer.authenticate
  load <- Printer.loadPage
  done <- Printer.printDocument(someDoc).recoverWith { case t: PartialPrintException => for {
    percent <- Printer.getValue("how much was printed?")
    error <- Printer.task(_ => Future.failed(new PartialPrintException(s"Printed $percent%")))
  } yield error
} yield done

/**
 * Execute our task
 * Note that everything up until now has worked on the fact that it will be
 * given a connection at some point while not actually having one. Here we
 * inject our connection and everything is finally applied.
 */
task.run(new UsbConnection()) // Future[_]
```


## License
The content of this library is released under the **MIT License** by
**Andrew Lawson**.<br/> You can find a copy of this license in
[`LICENSE`][license] or at http://opensource.org/licenses/mit.

[cats]: https://github.com/typelevel/cats
[travis]: https://travis-ci.org/adlawson/scala-cats
[ico-build]: http://img.shields.io/travis/adlawson/scala-cats/master.svg?style=flat
[license]: LICENSE
