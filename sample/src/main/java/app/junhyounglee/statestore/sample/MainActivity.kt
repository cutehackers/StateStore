package app.junhyounglee.statestore.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import app.junhyounglee.statestore.annotation.StateStore

class MainActivity : AppCompatActivity() {

  private val store = LocalStateStore()

  init {
    store.sample.observe(this) {
      Log.i("StateStore", "Sample StateStore: $it")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    findViewById<View>(R.id.button).setOnClickListener {
      store.increment()
    }
  }


  class LocalStateStore : AbsSampleStateStore() {
    init {
      _sample.value = 0
    }

    fun increment() {
      _sample.value?.also {
        _sample.value = it + 1
      }
    }
  }
}

interface SampleStateSpec {
  val sample: LiveData<Int>
}

@StateStore(stateSpec = SampleStateSpec::class)
class SampleStateStore { }

//@StateStore(stateSpec = SampleStateSpec::class)
//class SampleStateStore : AbsSampleStateStore() {
//
//  init {
//    _sample.value = 0
//  }
//
//  fun increment() {
//    _sample.value?.also {
//      _sample.value = it + 1
//    }
//  }
//}
