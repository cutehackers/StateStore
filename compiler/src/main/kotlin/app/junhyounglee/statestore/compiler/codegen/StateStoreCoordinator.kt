package app.junhyounglee.statestore.compiler.codegen

import app.junhyounglee.statestore.annotation.StateStore
import app.junhyounglee.statestore.compiler.StateContainerCoordinator
import app.junhyounglee.statestore.compiler.kotlinpoet.toClassName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
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

  private val targets = HashMap<KSClassDeclaration, KSValueArgument>()

  override fun process(
      resolver: Resolver,
      codeGenerator: CodeGenerator,
      logger: KSPLogger
  ): List<SourceGenerator<StateStoreSourceArguments>> {
    // visit @StateStore annotation class
    resolver.getSymbolsWithAnnotation(klassType.qualifiedName!!)
        .filter { it is KSClassDeclaration && it.validate() }
        .forEach { annotatedType ->
          annotatedType.accept(StateStoreVisitor(targets), Unit)
        }

    // parse if there are LiveData properties => ex) sample: LiveData<Int>
//    stateSpecType.getAllProperties().forEach { property: KSPropertyDeclaration ->
//      /*
//       * KSReferenceElement
//       *  - KSClassifierReference(DeclaredType)
//       *  - KSCallableReference(ExecutableType)
//       */
//      val propertyType = property.type.resolve()
//      // "LiveData"
//      val propertyTypeSimpleName = property.getTypeSimpleName()
//
//      /*
//       * val sample: LiveData<Int>
//       *  stateSpec: property.simpleName.asString()
//       *  androidx.lifecycle.LiveData: propertyType.declaration.qualifiedName?.asString()
//       *  kotlin.Int: propertyType.arguments.first().type?.resolve()?.declaration?.qualifiedName?.asString()
//       */
//    }

    /*
     * annotatedType == HelloStateStore or WorldStateStore
     * stateSpecArgument(@StateStore.spec) == HelloStateSpec  or WorldStateSpec
     */
    return targets.map { (annotatedType: KSClassDeclaration, valueArgument: KSValueArgument) ->
      logger.warn("StateStore> annotatedTargetType: ${annotatedType.qualifiedName?.asString()}, annotations: ${annotatedType.annotations.size}")
      logger.warn("StateStore> @StateStore argument = ${valueArgument.name?.asString()}")

      // TODO A batter way to handle code generation is to define generator here by type of StateStore
      //  annotation. We have only one annotation at the moment, so there is no need to handle
      //  different cases.

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

      // generate Abs{HelloStateStore|WorldStateSpec} class arguments with kotlin poet
      StateStoreSourceArguments.builder()
          .setSuperClassName(stateSpecType.toClassName())
          .setClassName(ClassName(annotatedType.packageName.asString(), "Abs${annotatedType.simpleName.asString()}"))
          .setOriginatingFiles(annotatedType.containingFile?.let { listOf(it) } ?: emptyList())
          .let {
            StateStoreSourceGenerator(it.build())
          }
    }
  }

  /**
   * A visitor for parsing interface arguments containing State LiveData properties.
   */
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
}
