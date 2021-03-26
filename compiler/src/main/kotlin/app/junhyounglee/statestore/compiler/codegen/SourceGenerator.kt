package app.junhyounglee.statestore.compiler.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset

abstract class SourceGenerator<ARGS : SourceArguments>(private val argument: ARGS) {

  @Throws(IOException::class)
  fun generate(codeGenerator: CodeGenerator) {
    /*
     * for custom class path can be set like this.
     *  val directory = File(folder, argument.getFileName())
     *  FileSpec.get(argument.className.packageName, klass).writeTo(directory)
     */
    val klass = onGenerate(argument)

    FileSpec.get(argument.className.packageName, klass)
        .writeTo(codeGenerator, argument.originatingFiles)
  }

  protected abstract fun onGenerate(argument: ARGS): TypeSpec

  private fun FileSpec.writeTo(codeGenerator: CodeGenerator, originatingFiles: List<KSFile>) {
    val dependencies = Dependencies(false, *originatingFiles.toTypedArray())
    val packageName = argument.className.packageName
    val fileName = argument.className.simpleName
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
