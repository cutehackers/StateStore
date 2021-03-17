package app.junhyounglee.statestore.compiler

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Origin
import kotlin.reflect.KClass

/**
 * Abstract coordinator class that parses state containers such as StateStore, StateStoreViewModel
 */
abstract class StateContainerCoordinator(internal val klassType: KClass<out Annotation>) {

  fun process(resolver: Resolver, logger: KSPLogger) {
    try {
      onProcess(resolver, logger)
    } catch (e: Throwable) {
      logger.exception(e)
    }
  }

  protected abstract fun onProcess(resolver: Resolver, logger: KSPLogger)
}

fun KSPropertyDeclaration.getTypeSimpleName() =
    if (type.element is KSClassifierReference && type.origin == Origin.KOTLIN) {
      (type.element as KSClassifierReference).referencedName()
    } else {
      ""
    }

