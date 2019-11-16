package com.morihacky.android.rxjava

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.morihacky.android.rxjava.fragments.MainFragment
import com.morihacky.android.rxjava.fragments.RotationPersist1WorkerFragment
import com.morihacky.android.rxjava.fragments.RotationPersist2WorkerFragment
import com.morihacky.android.rxjava.rxbus.RxBus

class MainActivity : AppCompatActivity() {

  private var _rxBus: RxBus? = null

  // This is better done with a DI Library like Dagger
  val rxBusSingleton = RxBus

  override fun onBackPressed() {
    super.onBackPressed()
    _removeWorkerFragments()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (savedInstanceState == null) {
      supportFragmentManager
          .beginTransaction()
          .replace(android.R.id.content, MainFragment(), this.toString())
          .commit()
    }
  }

  private fun _removeWorkerFragments() {
    var frag = supportFragmentManager
        .findFragmentByTag(RotationPersist1WorkerFragment::class.java.name)

    if (frag != null) {
      supportFragmentManager.beginTransaction()
          .remove(frag)
          .commit()
    }

    frag = supportFragmentManager
        .findFragmentByTag(RotationPersist2WorkerFragment::class.java.name)

    if (frag != null) {
      supportFragmentManager.beginTransaction()
          .remove(frag)
          .commit()
    }
  }
}
