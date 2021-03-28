package app.junhyounglee.statestore.compiler.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset

abstract class SourceGenerator<ARGS : SourceArguments>(private val arguments: ARGS) {

  @Throws(IOException::class)
  fun generate(codeGenerator: CodeGenerator, logger: KSPLogger) {
    /*
     * for custom class path can be set like this.
     *  val directory = File(folder, argument.getFileName())
     *  FileSpec.get(argument.className.packageName, klass).writeTo(directory)
     */
    val klass = onGenerate(arguments, logger)

    FileSpec.get(arguments.className.packageName, klass)
        .writeTo(codeGenerator, arguments)
  }

  protected abstract fun onGenerate(argument: ARGS, logger: KSPLogger): TypeSpec

  private fun FileSpec.writeTo(codeGenerator: CodeGenerator, arguments: SourceArguments) {
    val dependencies = Dependencies(true, *arguments.originatingFiles.toTypedArray())
    val packageName = arguments.className.packageName
    val fileName = arguments.className.simpleName
    val file = codeGenerator.createNewFile(dependencies, packageName, fileName)

    // Don't use writeTo(file) because that tries to handle directories under the hood
    OutputStreamWriter(file, Charset.forName("UTF-8"))
        .use(::writeTo)
  }


  companion object {
    const val DOCUMENTATION = "Auto generated class from StateStore"

    internal const val CORE_PACKAGE = "app.junhyounglee.statestore"
  }
}
