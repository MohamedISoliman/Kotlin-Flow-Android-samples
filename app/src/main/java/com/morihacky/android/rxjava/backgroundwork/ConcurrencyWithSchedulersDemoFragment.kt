package com.morihacky.android.rxjava.backgroundwork

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.morihacky.android.rxjava.R

import com.morihacky.android.rxjava.fragments.BaseFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_concurrency_schedulers.btn_start_operation
import kotlinx.android.synthetic.main.fragment_concurrency_schedulers.list_threading_log
import kotlinx.android.synthetic.main.fragment_concurrency_schedulers.progress_operation_running
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.collections.forEach

@ExperimentalCoroutinesApi
class ConcurrencyWithSchedulersDemoFragment : BaseFragment(){

  private var logs: MutableList<String> = mutableListOf()
  private val adapter: LogAdapter by lazy { LogAdapter(activity!!, mutableListOf()) }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setupLogger()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_concurrency_schedulers, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {

    btn_start_operation.setOnClickListener { startLongOperation() }
  }

  private fun startLongOperation() {
    getFlow().onStart {
      progress_operation_running.visibility = View.VISIBLE
    }
            .catch {
              Timber.e(it, "Error in RxJava Demo concurrency")
              log(String.format("Boo! Error %s", it.message))
            }
            .onEach {
              log(String.format("onNext with return value \"%b\"", it))
            }
            .onCompletion {
              log("On complete")
              progress_operation_running!!.visibility = View.INVISIBLE
            }
            .launchIn(this)
  }

  private fun getFlow(): Flow<Boolean> {
     return flowOf(true)
             .onEach { doSomeLongOperationThatBlocksCurrentThread() }
  }

  /**
   * Observer that handles the result through the 3 important actions:
   *
   *
   * 1. onCompleted 2. onError 3. onNext
   */
  private fun getDisposableObserver(): DisposableObserver<Boolean> {
    return object : DisposableObserver<Boolean>() {

      override fun onComplete() {
        log("On complete")
        progress_operation_running!!.visibility = View.INVISIBLE
      }

      override fun onError(e: Throwable) {
        Timber.e(e, "Error in RxJava Demo concurrency")
        log(String.format("Boo! Error %s", e.message))
        progress_operation_running!!.visibility = View.INVISIBLE
      }

      override fun onNext(bool: Boolean) {
        log(String.format("onNext with return value \"%b\"", bool))
      }
    }
  }

  // -----------------------------------------------------------------------------------
  // Method that help wiring up the example (irrelevant to RxJava)

  private suspend fun doSomeLongOperationThatBlocksCurrentThread() {
    log("performing long operation")

    try {
     delay(3000)
    } catch (e: InterruptedException) {
      Timber.d("Operation was interrupted")
    }

  }

  private fun log(logMsg: String) {

    if (isCurrentlyOnMainThread()) {
      logs.add(0, "$logMsg (main thread) ")
      adapter.clear()
      adapter.addAll(logs)
    } else {
      logs.add(0, "$logMsg (NOT main thread) ")

      // You can only do below stuff on main thread.
      Handler(Looper.getMainLooper())
          .post {
            adapter.clear()
            adapter.addAll(logs)
          }
    }
  }

  private fun setupLogger() {
    list_threading_log.adapter = adapter
  }

  private fun isCurrentlyOnMainThread(): Boolean {
    return Looper.myLooper() == Looper.getMainLooper()
  }

  private inner class LogAdapter(
    context: Context,
    logs: List<String>
  ) : ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}
