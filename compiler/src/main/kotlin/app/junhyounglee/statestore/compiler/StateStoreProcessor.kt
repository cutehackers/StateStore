package app.junhyounglee.statestore.compiler

import app.junhyounglee.statestore.annotation.StateStore
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * StateStore symbol processor
 *
 * following paths need to be marked as 'generated source root', as IDE doesn't know about generated
 * code.
 * build/generated/ksp/main/kotlin/
 * build/generated/ksp/main/java/
 * build/generated/ksp/main/resources
 *
 * interface SampleStateStore {
 *   val sample: LiveData<Int>
 * }
 *
 * @StateStore(store = SampleStateStore::class)
 * class RealSampleStateStore : AbsSampleStateStore {
 *
 * }
 *
 * @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
 * open class AbsSampleStateStore : SampleStateStore {
 *   override val sample: LiveData<Int>
 *     get() = _sample
 *   protected var _sample = MutableLiveData<Int>()
 * }
 *
 * Case 2)
 * @StateStoreViewModel(parent = ParentViewModel::class, store = SampleStateStore::class)
 * class RealSampleViewModel : AbsSampleViewModel {
 *
 * }
 *
 * open class AbsSampleViewModel : ParentViewModel(), SampleStateStore {
 *   override val sample: LiveData<Int>
 *     get() = _sample
 *   protected var _sample = MutableLiveData<Int>()
 * }
 */
class StateStoreProcessor : SymbolProcessor {

    lateinit var codeGenerator: CodeGenerator
    lateinit var logger: KSPLogger

    lateinit var coordinators: List<StateContainerCoordinator>

    override fun init(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.codeGenerator = codeGenerator
        this.logger = logger
        setUpOptions(options)
        setUpCoordinator()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        coordinators.forEach { it.process(resolver, logger) }

        return emptyList()
    }

    override fun finish() {

    }

    override fun onError() {

    }

    private fun setUpOptions(options: Map<String, String>) {
        logger.warn("StateStore> options: $options")
    }

    private fun setUpCoordinator() {
        coordinators = mutableListOf<StateStoreCoordinator>().apply {
            add(StateStoreCoordinator(StateStore::class))
        }
    }

    companion object {
        //private const val OPTIONS_DEBUGGABLE = "debuggable"
    }
}
