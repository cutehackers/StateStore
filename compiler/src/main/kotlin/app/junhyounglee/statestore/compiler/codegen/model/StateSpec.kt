package app.junhyounglee.statestore.compiler.codegen.model

import com.squareup.kotlinpoet.TypeName

data class StateSpec(val name: String, val parameterTypeName: TypeName)