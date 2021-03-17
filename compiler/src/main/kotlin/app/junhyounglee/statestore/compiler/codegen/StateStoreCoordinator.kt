package app.junhyounglee.statestore.compiler.codegen

import app.junhyounglee.statestore.annotation.StateStore
import app.junhyounglee.statestore.compiler.StateContainerCoordinator
import app.junhyounglee.statestore.compiler.getTypeSimpleName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import java.lang.Package.getPackage

/**
 * interface SampleStateSpec {
 *   val sample: LiveData<Int>
 * }
 *
 * @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
 * open class AbsSampleStateStore : SampleStateSpec {
 *   override val sample: LiveData<Int>
 *     get() = _sample
 *   protected var _sample = MutableLiveData<Int>()
 * }
 */
class StateStoreCoordinator : StateContainerCoordinator(StateStore::class) {

  private val visitor = StateStoreArgumentVisitor()
  private val arguments = mutableListOf<Any>()

  override fun onProcess(resolver: Resolver, logger: KSPLogger) {
    klassType.qualifiedName?.also { annotationName ->
      // visit @StateStore annotation class
      resolver.getSymbolsWithAnnotation(annotationName).forEach {
        it.annotations.single().arguments.map { argument: KSValueArgument ->
          argument.accept(visitor, Unit)
        }
      }

      arguments.forEach {
        // check if arguments are interface type
        val declaration: KSDeclaration? = (it as? KSType)?.declaration
        if ((declaration as? KSClassDeclaration)?.classKind != ClassKind.INTERFACE) {
          logger.error("Store type should be an interface. ${(it as KSType).declaration.qualifiedName?.asString()}")
        }

        val specType = declaration as KSClassDeclaration

        // parse if there are LiveData properties => ex) sample: LiveData<Int>
        specType.getAllProperties().forEach { property: KSPropertyDeclaration ->
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

      // TODO 아래의 빌더 클래스와 StateStoreSourceGenerator를 통해서 파일을 생성할 것. AbsSampleStateStore
//      val builder = StateStoreSourceArguments.builder()
//          .setClassName(ClassName(getPackage(annotatedType).qualifiedName.toString(), createClassName(annotatedType)))
//          .setTargetTypeName(getTargetTypeName(annotatedType))
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
