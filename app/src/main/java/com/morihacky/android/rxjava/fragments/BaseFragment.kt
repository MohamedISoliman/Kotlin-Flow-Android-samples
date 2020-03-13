package com.morihacky.android.rxjava.fragments

import androidx.fragment.app.Fragment
import com.morihacky.android.rxjava.MyApp

open class BaseFragment : Fragment() {
    
    override fun onDestroy() {
        super.onDestroy()
        val refWatcher = MyApp.getRefWatcher()
        refWatcher.watch(this)
    }
}