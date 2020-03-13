package com.morihacky.android.rxjava.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import co.kaush.core.util.CoreNullnessUtils
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.textChangesFlow
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.milliseconds

@FlowPreview
@ExperimentalCoroutinesApi
class DebounceSearchEmitterFragment : BaseFragment() {


    @JvmField
    @BindView(R.id.list_threading_log)
    var _logsList: ListView? = null

    @JvmField
    @BindView(R.id.input_txt_debounce)
    var _inputSearchText: EditText? = null
    private var _adapter: LogAdapter? = null
    private var _logs: MutableList<String>? = null
    private var _disposable: Disposable? = null
    private var unbinder: Unbinder? = null
    override fun onDestroy() {
        super.onDestroy()
        _disposable!!.dispose()
        unbinder!!.unbind()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_debounce, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    @OnClick(R.id.clr_debounce)
    fun onClearLog() {
        _logs = ArrayList()
        _adapter!!.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()

        _inputSearchText?.textChangesFlow()
                ?.debounce(400)
                ?.filter { it.string.isBlank().not() }
                ?.catch {
                    Timber.e(it, "--------- Woops on error!")
                    _log("Dang error. check your logs")
                }
                ?.onEach {
                    Timber.d("--------- onComplete")
                }
                ?.onCompletion {
                    Timber.d("--------- onComplete")
                }
                ?.launchIn(this)

        _disposable = RxTextView.textChangeEvents(_inputSearchText!!)
                .debounce(400, TimeUnit.MILLISECONDS) // default Scheduler is Computation
                .filter { changes: TextViewTextChangeEvent -> CoreNullnessUtils.isNotNullOrEmpty(changes.text().toString()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(_getSearchObserver())
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities
    private fun _getSearchObserver(): DisposableObserver<TextViewTextChangeEvent?> {
        return object : DisposableObserver<TextViewTextChangeEvent?>() {
            override fun onComplete() {
                Timber.d("--------- onComplete")
            }

            override fun onError(e: Throwable) {
                Timber.e(e, "--------- Woops on error!")
                _log("Dang error. check your logs")
            }

            override fun onNext(onTextChangeEvent: TextViewTextChangeEvent) {
                _log(String.format("Searching for %s", onTextChangeEvent.text().toString()))
            }
        }
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)
    private fun _setupLogger() {
        _logs = ArrayList()
        _adapter = LogAdapter(activity, ArrayList())
        _logsList!!.adapter = _adapter
    }

    private fun _log(logMsg: String) {
        if (_isCurrentlyOnMainThread()) {
            _logs?.add(0, "$logMsg (main thread) ")
            _adapter!!.clear()
            _adapter!!.addAll(_logs)
        } else {
            _logs?.add(0, "$logMsg (NOT main thread) ")

            // You can only do below stuff on main thread.
            Handler(Looper.getMainLooper())
                    .post {
                        _adapter!!.clear()
                        _adapter!!.addAll(_logs)
                    }
        }
    }

    private fun _isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private inner class LogAdapter(context: Context?, logs: List<String?>?) : ArrayAdapter<String?>(context, R.layout.item_log, R.id.item_log, logs)
}