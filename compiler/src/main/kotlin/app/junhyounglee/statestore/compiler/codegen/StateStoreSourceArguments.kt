package app.junhyounglee.statestore.compiler.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

class StateStoreSourceArguments(
    targetTypeName: TypeName,
    className: ClassName
) : SourceArguments(targetTypeName, className) {

  class Builder {
    private lateinit var targetTypeName: TypeName
    private lateinit var className: ClassName

    fun setTargetTypeName(targetTypeName: TypeName) = apply { this.targetTypeName = targetTypeName }
    fun setClassName(className: ClassName) = apply { this.className = className }

    fun isValid(): Boolean = this::targetTypeName.isInitialized
        && this::className.isInitialized

    fun build() = StateStoreSourceArguments(
        targetTypeName,
        className
    )
  }

  companion object {
    fun builder() = Builder()
  }
}