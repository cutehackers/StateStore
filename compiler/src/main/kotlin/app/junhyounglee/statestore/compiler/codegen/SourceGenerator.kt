package app.junhyounglee.statestore.compiler.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset

/**
 * Source code generation class. This class will write a kotlin code with KotlinPoet utility class.
 * KotlinPoet
 *  https://github.com/square/kotlinpoet
 */
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
        .writeTo(codeGenerator)
  }

  protected abstract fun onGenerate(argument: ARGS, logger: KSPLogger): TypeSpec

  private fun FileSpec.writeTo(codeGenerator: CodeGenerator) {
    val dependencies = Dependencies(true, *arguments.originatingFiles.toTypedArray())
    val file = codeGenerator.createNewFile(dependencies, packageName, name)
    // Don't use writeTo(file) because that tries to handle directories under the hood
    OutputStreamWriter(file, Charset.forName("UTF-8"))
        .use(::writeTo)
  }


  companion object {
    const val DOCUMENTATION = "Auto generated class from StateStore"

    internal const val BASE_PACKAGE = "app.junhyounglee.statestore"
  }
}
