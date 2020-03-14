package com.morihacky.android.rxjava.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.morihacky.android.rxjava.R
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import org.reactivestreams.Publisher
import org.reactivestreams.Subscription
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class PollingFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.list_threading_log)
    var _logsList: ListView? = null
    private var _adapter: LogAdapter? = null
    private var _counter = 0
    private var _disposables: CompositeDisposable? = null
    private val _logs = mutableListOf<String>()
    private var unbinder: Unbinder? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _disposables = CompositeDisposable()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_polling, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()
    }

    override fun onDestroy() {
        super.onDestroy()
        _disposables!!.clear()
        unbinder!!.unbind()
    }

    @OnClick(R.id.btn_start_simple_polling)
    fun onStartSimplePollingClicked() {
        val pollCount = POLL_COUNT
        val d = Flowable.interval(INITIAL_DELAY.toLong(), POLLING_INTERVAL.toLong(), TimeUnit.MILLISECONDS)
                .map { attempt: Long -> _doNetworkCallAndGetStringResult(attempt) }
                .take(pollCount.toLong())
                .doOnSubscribe { subscription: Subscription? -> _log(String.format("Start simple polling - %s", _counter)) }
                .subscribe { taskName: String? ->
                    _log(String.format(
                            Locale.US,
                            "Executing polled task [%s] now time : [xx:%02d]",
                            taskName,
                            _getSecondHand()))
                }
        _disposables!!.add(d)
    }

    @OnClick(R.id.btn_start_increasingly_delayed_polling)
    fun onStartIncreasinglyDelayedPolling() {
        _setupLogger()
        val pollingInterval = POLLING_INTERVAL
        val pollCount = POLL_COUNT
        _log(String.format(
                Locale.US, "Start increasingly delayed polling now time: [xx:%02d]", _getSecondHand()))
        _disposables!!.add(
                Flowable.just(1L)
                        .repeatWhen(RepeatWithDelay(pollCount, pollingInterval))
                        .subscribe(
                                { o: Long? ->
                                    _log(String.format(
                                            Locale.US,
                                            "Executing polled task now time : [xx:%02d]",
                                            _getSecondHand()))
                                }
                        ) { e: Throwable? -> Timber.d(e, "arrrr. Error") })
    }

    // -----------------------------------------------------------------------------------
    // CAUTION:
    // --------------------------------------
    // THIS notificationHandler class HAS NO BUSINESS BEING non-static
    // I ONLY did this cause i wanted access to the `_log` method from inside here
    // for the purpose of demonstration. In the real world, make it static and LET IT BE!!
    // It's 12am in the morning and i feel lazy dammit !!!
    private fun _doNetworkCallAndGetStringResult(attempt: Long): String {
        try {
            if (attempt == 4L) {
                // randomly make one event super long so we test that the repeat logic waits
                // and accounts for this.
                Thread.sleep(9000)
            } else {
                Thread.sleep(3000)
            }
        } catch (e: InterruptedException) {
            Timber.d("Operation was interrupted")
        }
        _counter++
        return _counter.toString()
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)
    private fun _getSecondHand(): Int {
        val millis = System.currentTimeMillis()
        return (TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))).toInt()
    }

    private fun _log(logMsg: String) {
        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, "$logMsg (main thread) ")
            _adapter!!.clear()
            _adapter!!.addAll(_logs)
        } else {
            _logs.add(0, "$logMsg (NOT main thread) ")

            // You can only do below stuff on main thread.
            Handler(Looper.getMainLooper())
                    .post {
                        _adapter!!.clear()
                        _adapter!!.addAll(_logs)
                    }
        }
    }

    private fun _setupLogger() {
        _adapter = LogAdapter(activity, ArrayList())
        _logsList!!.adapter = _adapter
        _counter = 0
    }

    private fun _isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    //public static class RepeatWithDelay
    inner class RepeatWithDelay internal constructor(private val _repeatLimit: Int, private val _pollingInterval: Int) : Function<Flowable<Any?>, Publisher<Long>> {
        private var _repeatCount = 1

        // this is a notificationhandler, all we care about is
        // the emission "type" not emission "content"
        // only onNext triggers a re-subscription
        @Throws(Exception::class)
        override fun apply(inputFlowable: Flowable<Any?>): Publisher<Long> {
            // it is critical to use inputObservable in the chain for the result
            // ignoring it and doing your own thing will break the sequence
            return inputFlowable.flatMap(
                    object : Function<Any?, Publisher<Long>> {
                        @Throws(Exception::class)
                        override fun apply(t: Any): Publisher<Long> {
                            if (_repeatCount >= _repeatLimit) {
                                // terminate the sequence cause we reached the limit
                                _log("Completing sequence")
                                return Flowable.empty()
                            }

                            // since we don't get an input
                            // we store state in this handler to tell us the point of time we're firing
                            _repeatCount++
                            return Flowable.timer(_repeatCount * _pollingInterval.toLong(), TimeUnit.MILLISECONDS)
                        }

                    })
        }

    }

    private inner class LogAdapter(context: Context?, logs: List<String?>?) : ArrayAdapter<String?>(context, R.layout.item_log, R.id.item_log, logs)
    companion object {
        private const val INITIAL_DELAY = 0
        private const val POLLING_INTERVAL = 1000
        private const val POLL_COUNT = 8
    }
}