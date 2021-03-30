package app.junhyounglee.statestore.compiler.codegen

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName

class StateStoreSourceArguments(
    val superClassName: ClassName,
    className: ClassName,
    originatingFiles: List<KSFile> = emptyList()
) : SourceArguments(className, originatingFiles) {

  class Builder {
    private lateinit var className: ClassName
    private lateinit var superClassName: ClassName
    private var originatingFiles: List<KSFile> = emptyList()

    fun setClassName(className: ClassName) = apply { this.className = className }
    fun setSuperClassName(superClassName: ClassName) = apply { this.superClassName = superClassName }
    fun setOriginatingFiles(originatingFiles: List<KSFile>) = apply { this.originatingFiles = originatingFiles }

    fun isValid(): Boolean = this::superClassName.isInitialized
        && this::className.isInitialized

    fun build() = StateStoreSourceArguments(superClassName, className, originatingFiles)
  }

  companion object {
    fun builder() = Builder()
  }
}