package app.junhyounglee.statestore.compiler.codegen

import app.junhyounglee.statestore.annotation.StateStore
import app.junhyounglee.statestore.compiler.StateContainerCoordinator
import app.junhyounglee.statestore.compiler.getTypeSimpleName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName

/**
 * interface SampleStateSpec {
 *   val sample: LiveData<Int>
 * }
 *
 * @StateStore(stateSpec = SampleStateSpec::class)
 * class SampleStateStore : AbsSampleStateStore {
 *
 * }
 */
class StateStoreCoordinator : StateContainerCoordinator(StateStore::class) {

  private val visitor = StateStoreArgumentVisitor()
  private var annotatedType: KSClassDeclaration? = null
  private val arguments = mutableListOf<Any>()
  private val targets = mutableListOf<KSClassDeclaration>()

  override fun onProcess(
      resolver: Resolver,
      logger: KSPLogger
  ): SourceGenerator<out SourceArguments> {
    val annotationName = klassType.qualifiedName ?: throw IllegalStateException("Illegal type of annotation class.")

    // visit @StateStore annotation class
    resolver.getSymbolsWithAnnotation(annotationName)
        .filter { it is KSClassDeclaration && it.validate() }
        .forEach {
          val annotatedType = it as KSClassDeclaration
          // annotatedType == SampleStateStore
          //val annotation: KSAnnotation? = annotatedType.annotations.find { it.annotationType.resolve() ==  KSType(StateStore::class) }
          this.annotatedType = annotatedType
          annotatedType.annotations.single().arguments.map { argument: KSValueArgument ->
            argument.accept(visitor, Unit)
          }
        }

    // annotatedType == SampleStateStore
    val annotatedType = this.annotatedType
        ?: throw IllegalStateException("Annotated class should not be a valid class.")
    // argument.stateSpec == SampleStateSpec
    var stateSpec: ClassName? = null

    val argument = arguments.singleOrNull() ?: throw IllegalStateException("StateStore should have a stateSpec interface.")
    // check if arguments are interface type
    val declaration: KSDeclaration? = (argument as? KSType)?.declaration
    if ((declaration as? KSClassDeclaration)?.classKind != ClassKind.INTERFACE) {
      logger.error("Store type should be an interface. ${(argument as KSType).declaration.qualifiedName?.asString()}")
    }

    val stateSpecType = declaration as KSClassDeclaration
    stateSpec = stateSpecType.let {
      ClassName(it.packageName.asString(), it.simpleName.asString())
    }

    // parse if there are LiveData properties => ex) sample: LiveData<Int>
    stateSpecType.getAllProperties().forEach { property: KSPropertyDeclaration ->
      /*
       * KSReferenceElement
       *  - KSClassifierReference(DeclaredType)
       *  - KSCallableReference(ExecutableType)
       */
      val propertyType = property.type.resolve()
      // "LiveData"
      val propertyTypeSimpleName = property.getTypeSimpleName()

      /*
       * val sample: LiveData<Int>
       *  stateSpec: property.simpleName.asString()
       *  androidx.lifecycle.LiveData: propertyType.declaration.qualifiedName?.asString()
       *  kotlin.Int: propertyType.arguments.first().type?.resolve()?.declaration?.qualifiedName?.asString()
       */
    }

    val myClass = ClassName(annotatedType.packageName.asString(), createClassName(annotatedType))

    logger.warn("originatingFile:  ${annotatedType.containingFile}")
    logger.warn("myClass: ${myClass.packageName}.${myClass.simpleName}")

    val superClassName = stateSpec ?: throw IllegalStateException("Invalid stateSpec class argument found!")

    return StateStoreSourceArguments.builder()
        .setSuperClassName(superClassName)
        .setClassName(ClassName(annotatedType.packageName.asString(), createClassName(annotatedType)))
        .setOriginatingFiles(annotatedType.containingFile?.let { listOf(it) } ?: emptyList())
        .let {
          StateStoreSourceGenerator(it.build())
        }
  }

  /**
   * A visitor for parsing interface arguments containing State LiveData properties.
   */
  inner class StateStoreArgumentVisitor : KSVisitorVoid() {
    override fun visitValueArgument(valueArgument: KSValueArgument, data: Unit) {
      valueArgument.value?.also { arguments.add(it) }
    }
  }
}
