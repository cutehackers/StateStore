# StateStore

### Case 1)
```kotlin
interface SampleStateSpec {
  val sample: LiveData<Int>
}

@StateStore(spec = SampleStateSpec::class)
class SampleStateStore : AbsSampleStateStore {

}

@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
open class AbsSampleStateStore : SampleStateSpec {
  override val sample: LiveData<Int>
    get() = _sample
  protected var _sample = MutableLiveData<Int>()
}
```

### Case 2)
```kotlin
@StateStoreViewModel(parent = ParentViewModel::class, store = SampleStateSpec::class)
class RealSampleViewModel : AbsSampleViewModel {

}

open class AbsSampleViewModel : ParentViewModel(), SampleStateSpec {
  override val sample: LiveData<Int>
    get() = _sample
  protected var _sample = MutableLiveData<Int>()
}
```