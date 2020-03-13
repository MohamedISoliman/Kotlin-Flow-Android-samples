package com.morihacky.android.rxjava.fragments

import androidx.fragment.app.Fragment
import com.morihacky.android.rxjava.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

open class BaseFragment : Fragment(), CoroutineScope by CoroutineScope(Dispatchers.Main) {

    override fun onDestroy() {
        super.onDestroy()
        val refWatcher = MyApp.getRefWatcher()
        refWatcher.watch(this)
    }
}