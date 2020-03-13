package com.morihacky.android.rxjava

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.morihacky.android.rxjava.rxbus.RxBus.send
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 *
 * Created by Mohamed Ibrahim on 3/13/20.
 */
data class TextViewTextChangeEvent(val string: String)

fun EditText.textChangesFlow(): Flow<TextViewTextChangeEvent> {
    return channelFlow {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                launch {
                    send(TextViewTextChangeEvent(s.toString()))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        addTextChangedListener(watcher)
        awaitClose { removeTextChangedListener(watcher) }
    }
}