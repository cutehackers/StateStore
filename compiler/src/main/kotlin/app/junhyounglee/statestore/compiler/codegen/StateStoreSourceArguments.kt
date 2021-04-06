package app.junhyounglee.statestore.compiler.codegen

import app.junhyounglee.statestore.compiler.codegen.model.StateSpec
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName

class StateStoreSourceArguments(
    val superClassName: ClassName,
    val stateSpecs: List<StateSpec>,
    className: ClassName,
    originatingFiles: List<KSFile> = emptyList()
) : SourceArguments(className, originatingFiles) {

  class Builder {
    private lateinit var className: ClassName
    private lateinit var superClassName: ClassName
    private lateinit var stateSpecs: List<StateSpec>
    private var originatingFiles: List<KSFile> = emptyList()

    fun setClassName(className: ClassName) = apply { this.className = className }
    fun setSuperClassName(superClassName: ClassName) = apply { this.superClassName = superClassName }
    fun setStateSpecs(stateSpecs: List<StateSpec>) = apply { this.stateSpecs = stateSpecs }
    fun setOriginatingFiles(originatingFiles: List<KSFile>) = apply { this.originatingFiles = originatingFiles }

    fun isValid(): Boolean = this::superClassName.isInitialized
        && this::className.isInitialized
        && this::stateSpecs.isInitialized

    fun build() = StateStoreSourceArguments(superClassName, stateSpecs, className, originatingFiles)
  }

  companion object {
    fun builder() = Builder()
  }
}