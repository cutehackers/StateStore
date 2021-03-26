package app.junhyounglee.statestore.compiler.codegen

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * @param className virtual class name that is going to be created by annotation processor
 */
open class SourceArguments(val className: ClassName, val originatingFiles: List<KSFile>)