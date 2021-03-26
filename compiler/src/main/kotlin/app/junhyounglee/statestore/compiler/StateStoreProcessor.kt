package app.junhyounglee.statestore.compiler

import app.junhyounglee.statestore.annotation.StateStore
import app.junhyounglee.statestore.compiler.codegen.StateStoreCoordinator
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
 * interface SampleStateSpec {
 *   val sample: LiveData<Int>
 * }
 *
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
 *  - https://medium.com/@jsuch2362/my-first-kotlin-symbol-processing-tool-for-android-4eb3a2cfd600
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
    //coordinators.forEach { it.process(resolver, logger) }

    val annotatedTypes: List<KSClassDeclaration> =
        resolver.getSymbolsWithAnnotation(StateStore::class.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .map { it as KSClassDeclaration }

    if (annotatedTypes.isEmpty()) {
      return emptyList()
    }

    // 1. file generation test
    val sampleTargetClass = annotatedTypes.first()
    val output = codeGenerator.createNewFile(
        dependencies = Dependencies(false, *annotatedTypes.map { it.containingFile!! }.toTypedArray()),
        packageName = sampleTargetClass.packageName.asString(),
        fileName = "Mvl${sampleTargetClass.simpleName.asString()}"
    )
    output.use {
      it.appendText("package ${sampleTargetClass.packageName.asString()}\n\n")
      it.appendText("abstract class Mvl${sampleTargetClass.simpleName.asString()} {\n")
      it.appendText("}")
    }

    val stateStoreAnnotationType: KSType = resolver.getClassDeclarationByName(
        "app.junhyounglee.statestore.annotation.StateStore"
    )!!.asType(emptyList())

    // 2. parsing annotated class type test
    annotatedTypes.forEach { target ->
      logger.warn("StateStore> target class name: ${target.qualifiedName?.asString()}, annotations: ${target.annotations.size}")

      target.annotations
          .find {
            //it.annotationType.resolve() == stateStoreAnnotationType
            logger.warn("StateStore. isStateStoreAnnotation: ${it.annotationType.resolve().declaration.qualifiedName?.asString()}")
            it.shortName.asString() == "StateStore"
          }
          ?.let { ksAnnotation ->
            logger.warn("StateStore> argument: ${ksAnnotation.arguments.first().name?.asString() ?: "NONAME"}, arguments size: ${ksAnnotation.arguments.size}")
            ksAnnotation.arguments.find { it.name?.asString() == "stateSpec" }
          }?.also { ksValueArgument ->
            logger.warn("StateStore> StateSpec argument name = ${ksValueArgument.name?.asString()}")
          }
    }

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
      add(StateStoreCoordinator())
    }
  }

  private fun Resolver.getClassDeclarationByName(fullyQualifiedName: String): KSClassDeclaration? =
      getClassDeclarationByName(getKSNameFromString(fullyQualifiedName))

  inner class StateStoreVisitor : KSVisitorVoid() {
    override fun visitValueArgument(valueArgument: KSValueArgument, data: Unit) {
      //valueArgument.value?.also { arguments.add(it) }
    }
  }

  companion object {
    //private const val OPTIONS_DEBUGGABLE = "debuggable"
  }
}

fun OutputStream.appendText(str: String) {
  this.write(str.toByteArray())
}
