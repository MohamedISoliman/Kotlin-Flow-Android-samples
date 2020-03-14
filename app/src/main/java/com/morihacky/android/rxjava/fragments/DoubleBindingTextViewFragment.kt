package com.morihacky.android.rxjava.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnTextChanged
import butterknife.Unbinder
import com.morihacky.android.rxjava.R
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.*
import org.intellij.lang.annotations.Flow

@ExperimentalCoroutinesApi
class DoubleBindingTextViewFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.double_binding_num1)
    var _number1: EditText? = null

    @JvmField
    @BindView(R.id.double_binding_num2)
    var _number2: EditText? = null

    @JvmField
    @BindView(R.id.double_binding_result)
    var _result: TextView? = null
    var _resultEmitterSubject = BroadcastChannel<Float>(1)
    private var unbinder: Unbinder? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_double_binding_textview, container, false)
        unbinder = ButterKnife.bind(this, layout)


        _resultEmitterSubject.asFlow()
                .onEach {
                    _result!!.text = it.toString()

                }.launchIn(this)

        onNumberChanged()
        _number2!!.requestFocus()
        return layout
    }

    @OnTextChanged(R.id.double_binding_num1, R.id.double_binding_num2)
    fun onNumberChanged() {
        var num1 = 0f
        var num2 = 0f
        if (!TextUtils.isEmpty(_number1!!.text.toString())) {
            num1 = _number1!!.text.toString().toFloat()
        }
        if (!TextUtils.isEmpty(_number2!!.text.toString())) {
            num2 = _number2!!.text.toString().toFloat()
        }
        _resultEmitterSubject.offer(num1 + num2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }
}