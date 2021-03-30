package app.junhyounglee.statestore.compiler

import app.junhyounglee.statestore.compiler.codegen.SourceArguments
import app.junhyounglee.statestore.compiler.codegen.SourceGenerator
import app.junhyounglee.statestore.compiler.codegen.StateStoreCoordinator
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * StateStore symbol processor
 *
 * following paths need to be marked as 'generated source root', as IDE doesn't know about generated
 * code.
 * build/generated/ksp/main/kotlin/
 * build/generated/ksp/main/java/
 * build/generated/ksp/main/resources
 *
 * interface SampleStateSpec {
 *   val sample: LiveData<Int>
 * }
 *
 * Case 1)
 * @StateStore(spec = SampleStateSpec::class)
 * class SampleStateStore : AbsSampleStateStore {
 *
 * }
 *
 * @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
 * open class AbsSampleStateStore : SampleStateSpec {
 *   override val sample: LiveData<Int>
 *     get() = _sample
 *   protected var _sample = MutableLiveData<Int>()
 * }
 *
 * Case 2)
 * @StateStoreViewModel(parent = ParentViewModel::class, store = SampleStateSpec::class)
 * class RealSampleViewModel : AbsSampleViewModel {
 *
 * }
 *
 * open class AbsSampleViewModel : ParentViewModel(), SampleStateSpec {
 *   override val sample: LiveData<Int>
 *     get() = _sample
 *   protected var _sample = MutableLiveData<Int>()
 * }
 *
 * refs
 *  - https://github.com/SeongUgJung/KspSample
 *  - https://medium.com/@jsuch2362/my-first-kotlin-symbol-processing-tool-for-android-4eb3a2cfd600
 */
class StateStoreProcessor : SymbolProcessor {

  lateinit var codeGenerator: CodeGenerator
  lateinit var logger: KSPLogger

  private val coordinators: List<StateContainerCoordinator> = listOf(
      StateStoreCoordinator()
  )

  override fun init(
      options: Map<String, String>,
      kotlinVersion: KotlinVersion,
      codeGenerator: CodeGenerator,
      logger: KSPLogger
  ) {
    this.codeGenerator = codeGenerator
    this.logger = logger
    setUpOptions(options)
  }

  private val generators = mutableListOf<List<SourceGenerator<out SourceArguments>>>()

  override fun process(resolver: Resolver): List<KSAnnotated> {
    // NOTE! stack overflow will happen if this is called:
    coordinators.forEach {
      logger.warn("coordinator is working ...")
      generators.add(it.process(resolver, codeGenerator, logger))
    }

    /*
     * How to resolve kotlin symbol type from qualified name of type.
     *
     * val stateStoreAnnotationType: KSType = resolver.getClassDeclarationByName(
     *   "app.junhyounglee.statestore.annotation.StateStore"
     * )!!.asType(emptyList())
     */

    return emptyList()
  }

  override fun finish() {
    /*
     * Note!
     * Using codeGenerator.createNewFile() triggers stack-overflow exception, which I don't know
     * what the exact reason is. But running it here in finish() callback is good enough.
     * SymbolProcessor.process()에서 codeGenerator.createNewFile을 사용할 경우 stack-overflow가 발생한다.
     * 따라서 코드를 생성하는 코드는 finish() 루틴에서 실행한다.
     */
    generators.flatten().forEach {
      it.generate(codeGenerator, logger)
    }
  }

  override fun onError() {

  }

  private fun setUpOptions(options: Map<String, String>) {
    logger.warn("StateStore> options: $options")
  }

  private fun Resolver.getClassDeclarationByName(fullyQualifiedName: String): KSClassDeclaration? =
      getClassDeclarationByName(getKSNameFromString(fullyQualifiedName))

  companion object {
    //private const val OPTIONS_DEBUGGABLE = "debuggable"
  }
}
