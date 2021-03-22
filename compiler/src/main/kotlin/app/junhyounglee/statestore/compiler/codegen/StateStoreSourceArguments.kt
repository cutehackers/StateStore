package app.junhyounglee.statestore.compiler.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

class StateStoreSourceArguments(
    className: ClassName,
    val superClassName: ClassName
) : SourceArguments(className) {

  class Builder {
    private lateinit var className: ClassName
    private lateinit var superClassName: ClassName

    fun setClassName(className: ClassName) = apply { this.className = className }
    fun setSuperClassName(superClassName: ClassName) = apply { this.superClassName = superClassName }

    fun isValid(): Boolean = this::superClassName.isInitialized
        && this::className.isInitialized

    fun build() = StateStoreSourceArguments(className, superClassName)
  }

  companion object {
    fun builder() = Builder()
  }
}