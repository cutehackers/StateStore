package app.junhyounglee.statestore.compiler.codegen

import app.junhyounglee.statestore.annotation.StateStore
import app.junhyounglee.statestore.compiler.StateContainerCoordinator
import app.junhyounglee.statestore.compiler.getTypeSimpleName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
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

  override fun onProcess(
      resolver: Resolver,
      logger: KSPLogger
  ): SourceGenerator<out SourceArguments> {
    val annotationName = klassType.qualifiedName ?: throw IllegalStateException("Illegal type of annotation class.")

    // visit @StateStore annotation class
    resolver.getSymbolsWithAnnotation(annotationName)
        .filterIsInstance<KSClassDeclaration>()
        .forEach { annotatedType: KSClassDeclaration ->
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

    arguments.forEach {
      // check if arguments are interface type
      val declaration: KSDeclaration? = (it as? KSType)?.declaration
      if ((declaration as? KSClassDeclaration)?.classKind != ClassKind.INTERFACE) {
        logger.error("Store type should be an interface. ${(it as KSType).declaration.qualifiedName?.asString()}")
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
    }

    val superClassName = stateSpec ?: throw IllegalStateException("Invlaid stateSpec class argument found!")

    // TODO 아래의 빌더 클래스와 StateStoreSourceGenerator를 통해서 파일을 생성할 것. AbsSampleStateStore
    return StateStoreSourceArguments.builder()
        .setClassName(ClassName(annotatedType.packageName.asString(), createClassName(annotatedType)))
        .setSuperClassName(superClassName)
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
