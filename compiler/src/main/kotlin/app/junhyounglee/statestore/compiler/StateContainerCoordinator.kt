package app.junhyounglee.statestore.compiler

import app.junhyounglee.statestore.compiler.codegen.SourceArguments
import app.junhyounglee.statestore.compiler.codegen.SourceGenerator
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Origin
import kotlin.reflect.KClass

/**
 * Abstract coordinator class that parses state containers such as StateStore, StateStoreViewModel
 */
abstract class StateContainerCoordinator(internal val klassType: KClass<out Annotation>) {

  abstract fun process(
      resolver: Resolver,
      codeGenerator: CodeGenerator,
      logger: KSPLogger
  ): List<SourceGenerator<out SourceArguments>>

  fun createClassName(annotatedType: KSClassDeclaration): String = annotatedType.simpleName.asString().let {
    if (it.isEmpty()) {
      ""
    } else {
      "Abs${it}"
    }
  }

}

@Suppress("unused")
fun KSPropertyDeclaration.getTypeSimpleName() =
    if (type.element is KSClassifierReference && type.origin == Origin.KOTLIN) {
      (type.element as KSClassifierReference).referencedName()
    } else {
      ""
    }

