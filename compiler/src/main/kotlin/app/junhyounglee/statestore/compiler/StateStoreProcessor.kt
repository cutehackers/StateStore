package app.junhyounglee.statestore.compiler

import app.junhyounglee.statestore.annotation.StateStore
import app.junhyounglee.statestore.compiler.codegen.SourceGenerator
import app.junhyounglee.statestore.compiler.codegen.StateStoreCoordinator
import app.junhyounglee.statestore.compiler.codegen.StateStoreSourceArguments
import app.junhyounglee.statestore.compiler.kotlinpoet.toClassName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

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

  lateinit var coordinators: List<StateContainerCoordinator>

  private val targets = HashMap<KSClassDeclaration, KSValueArgument>()

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

    // visit @StateStore annotation class
    resolver.getSymbolsWithAnnotation(StateStore::class.qualifiedName!!)
        .filter { it is KSClassDeclaration && it.validate() }
        .forEach { annotatedType ->
          annotatedType.accept(StateStoreVisitor(targets), Unit)
        }

//    val stateStoreAnnotationType: KSType = resolver.getClassDeclarationByName(
//        "app.junhyounglee.statestore.annotation.StateStore"
//    )!!.asType(emptyList())

    return emptyList()
  }

  override fun finish() {
    /*
     * annotatedType == HelloStateStore or WorldStateStore
     * stateSpecArgument(@StateStore.spec) == HelloStateSpec  or WorldStateSpec
     */
    targets.forEach { (target: KSClassDeclaration, valueArgument: KSValueArgument) ->
      logger.warn("StateStore> annotatedTargetType: ${target.qualifiedName?.asString()}, annotations: ${target.annotations.size}")
      logger.warn("StateStore> @StateStore argument = ${valueArgument.name?.asString()}")

      val stateSpec = valueArgument.value ?: IllegalStateException("StateStore should have stateSpec interface.")

      // check if arguments are interface type
      val declaration: KSDeclaration? = (stateSpec as? KSType)?.declaration
      if ((declaration as? KSClassDeclaration)?.classKind != ClassKind.INTERFACE) {
        logger.error("Store type should be an interface. ${(stateSpec as KSType).declaration.qualifiedName?.asString()}")
      }

      // @StateStore.stateSpec interface type
      val stateSpecType = declaration as KSClassDeclaration

      // parse if there are LiveData properties => ex) sample: LiveData<Int>
      //...

      // generate Abs{HelloStateStore|WorldStateSpec} class with kotlin poet
      val arguments = StateStoreSourceArguments.builder()
          .setSuperClassName(stateSpecType.toClassName())
          .setClassName(ClassName(target.packageName.asString(), "Abs${target.simpleName.asString()}"))
          .setOriginatingFiles(target.containingFile?.let { listOf(it) } ?: emptyList())
          .build()
      val klass: TypeSpec = onGenerate(arguments)

      FileSpec.get(arguments.className.packageName, klass)
          .writeTo(codeGenerator, arguments)
    }
  }

  private fun onGenerate(argument: StateStoreSourceArguments): TypeSpec {
    val builder = TypeSpec.classBuilder(argument.className.simpleName)
        .addKdoc(SourceGenerator.DOCUMENTATION)
        .addModifiers(KModifier.PUBLIC)
        .addModifiers(KModifier.ABSTRACT)
        .addSuperinterface(argument.superClassName)
    return builder.build()
  }

  private fun FileSpec.writeTo(
      codeGenerator: CodeGenerator,
      arguments: StateStoreSourceArguments
  ) {
    val dependencies = Dependencies(true, *arguments.originatingFiles.toTypedArray())
    val packageName = arguments.className.packageName
    val fileName = arguments.className.simpleName
    val file = codeGenerator.createNewFile(dependencies, packageName, fileName)

    // Don't use writeTo(file) because that tries to handle directories under the hood
    OutputStreamWriter(file, Charset.forName("UTF-8"))
        .use(::writeTo)
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

  class StateStoreVisitor(
      private val targets: HashMap<KSClassDeclaration, KSValueArgument>
  ) : KSVisitorVoid() {
    private val visited = hashSetOf<Any>()

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
      classDeclaration.annotations
          .firstOrNull { annotation: KSAnnotation ->
            // precise type comparison -> it.annotationType.resolve() == stateStoreAnnotationType
            annotation.shortName.asString() == "StateStore"
          }
          ?.arguments?.firstOrNull {
            it.name?.asString() == "stateSpec"
          }
          ?.also {
            targets[classDeclaration] = it
          }
    }

    // used to check if the symbol is already visited by using accept method.
    private fun hasVisited(symbol: Any): Boolean {
      return if (visited.contains(symbol)) {
        true
      } else {
        visited.add(symbol)
        false
      }
    }
  }

  companion object {
    //private const val OPTIONS_DEBUGGABLE = "debuggable"
  }
}

fun OutputStream.appendText(str: String) {
  this.write(str.toByteArray())
}
