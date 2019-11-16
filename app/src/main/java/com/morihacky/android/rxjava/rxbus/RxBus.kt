package com.morihacky.android.rxjava.rxbus

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

/** courtesy: https://gist.github.com/benjchristensen/04eef9ca0851f3a5d7bf  */
object RxBus {

  private val _bus = PublishRelay.create<Any>()
      .toSerialized()

  fun send(o: Any) {
    _bus.accept(o)
  }

  fun asFlowable(): Flowable<Any> {
    return _bus.toFlowable(BackpressureStrategy.LATEST)
  }

  fun hasObservers(): Boolean {
    return _bus.hasObservers()
  }
}
