package app.junhyounglee.statestore.compiler

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import kotlin.reflect.KClass

interface StateContainerCoordinator {
    fun process(resolver: Resolver, logger: KSPLogger)
}

fun KSPropertyDeclaration.getPropertyTypeSimpleName() =
    if (type.element is KSClassifierReference && type.origin == Origin.KOTLIN) {
        (type.element as KSClassifierReference).referencedName()
    } else {
        ""
    }

class StateStoreCoordinator(
    private val klassType: KClass<out Annotation>
) : StateContainerCoordinator {
    private val visitor = StateStoreArgumentVisitor()
    private val arguments = mutableListOf<Any>()

    override fun process(resolver: Resolver, logger: KSPLogger) {
        klassType.qualifiedName?.also { annotationName ->
            // visit @StateStore annotation
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

                val storeType = declaration as KSClassDeclaration

                // parse if there are LiveData properties => ex) sample: LiveData<Int>
                storeType.getAllProperties().forEach { property: KSPropertyDeclaration ->
                    /*
                     * KSReferenceElement
                     *  - KSClassifierReference(DeclaredType)
                     *  - KSCallableReference(ExecutableType)
                     */
                    val propertyType = property.type.resolve()
                    // "LiveData"
                    val propertyTypeSimpleName = property.getPropertyTypeSimpleName()

                    /*
                     * val sample: LiveData<Int>
                     *  sample:  property.simpleName.asString()
                     *  androidx.lifecycle.LiveData: propertyType.declaration.qualifiedName?.asString()
                     *  kotlin.Int: propertyType.arguments.first().type?.resolve()?.declaration?.qualifiedName?.asString()
                     */
                }
            }
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
