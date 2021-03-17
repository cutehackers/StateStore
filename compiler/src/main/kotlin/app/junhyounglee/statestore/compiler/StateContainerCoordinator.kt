package app.junhyounglee.statestore.compiler

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import kotlin.reflect.KClass

abstract class StateContainerCoordinator(internal val klassType: KClass<out Annotation>) {

    fun process(resolver: Resolver, logger: KSPLogger) {
        try {
            onProcess(resolver, logger)
        } catch (e: Throwable) {
            logger.exception(e)
        }
    }

    abstract fun onProcess(resolver: Resolver, logger: KSPLogger)
}

fun KSPropertyDeclaration.getPropertyTypeSimpleName() =
    if (type.element is KSClassifierReference && type.origin == Origin.KOTLIN) {
        (type.element as KSClassifierReference).referencedName()
    } else {
        ""
    }

class StateStoreCoordinator(klassType: KClass<out Annotation>)
    : StateContainerCoordinator(klassType) {

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
                    val propertyTypeSimpleName = property.getPropertyTypeSimpleName()

                    /*
                     * val sample: LiveData<Int>
                     *  stateSpec: property.simpleName.asString()
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
