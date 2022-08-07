package shop.retries

import cats.effect.Temporal
import cats.syntax.show.*
import org.typelevel.log4cats.Logger
import retry.RetryDetails.*
import retry.*

trait Retry[F[_]] {
  def retry[A](policy:RetryPolicy[F],retriable:Retriable)(fa:F[A]):F[A]
}

object Retry{
    def apply[F[_]:Retry]:Retry[F]=summon

    given  forLoggerTemporal[F[_]:Logger:Temporal]:Retry[F]= new Retry[F]{
        def retry[A](policy:RetryPolicy[F],retriable:Retriable)(fa:F[A]):F[A]={
            def onError(e:Throwable,details:RetryDetails):F[Unit]={
                details match {
                    case WillDelayAndRetry(_,retries,_) =>Logger[F].error(s" Fail to process ${retriable.show} We retried ${retries} times")
                    case GivingUp(total,_)              =>Logger[F].error(s"Giving up after ${total} retries on ${retriable.show}")
                }                
            }

            retryingOnAllErrors(policy,onError)(fa)
        }    
    }
}