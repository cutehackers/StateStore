package app.junhyounglee.statestore.compiler.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * @param targetTypeName annotated class type name
 * @param className virtual class name that is going to be created by annotation processor
 */
open class SourceArguments(val targetTypeName: TypeName, val className: ClassName)
