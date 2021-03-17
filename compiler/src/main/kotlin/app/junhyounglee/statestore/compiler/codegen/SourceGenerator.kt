package app.junhyounglee.statestore.compiler.codegen

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import java.io.IOException

abstract class SourceGenerator<ARGS : SourceArguments>() {

    @Throws(IOException::class)
    fun generate(argument: ARGS) {
        /*
         * for custom class path can be set like this.
         *  val file = File(folder, argument.getFileName())
         *  FileSpec.get(argument.className.packageName, klass).writeTo(file)
         */
        val klass = onGenerate(argument)

        try {
            FileSpec.get(argument.className.packageName, klass).writeTo(File("{SOURCE_FILE_PATH}"))

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    abstract fun onGenerate(argument: ARGS): TypeSpec


    companion object {
        const val DOCUMENTATION = "Auto generated class from StateStore"

        internal const val CORE_PACKAGE = "app.junhyounglee.statestore"
    }
}
