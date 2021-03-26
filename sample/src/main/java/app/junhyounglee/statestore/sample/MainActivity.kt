package app.junhyounglee.statestore.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LiveData
import app.junhyounglee.statestore.annotation.StateStore

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }
}

interface HelloStateSpec {
  val sample: LiveData<Int>
}
interface WorldStateSpec {
  val sample: LiveData<Int>
}

@StateStore(HelloStateSpec::class)
class HelloStateStore

@StateStore(WorldStateSpec::class)
class WorldStateStore
