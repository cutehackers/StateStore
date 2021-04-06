package app.junhyounglee.statestore.compiler.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

/**
 * Source code generator of @StateStore annotated class. This will only write a kotlin code with
 * given code arguments.
 *
 * interface SampleStateSpec {
 *   val sample: LiveData<Int>
 * }
 *
 * @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
 * open class AbsSampleStateStore : SampleStateSpec {
 *   override val sample: LiveData<Int>
 *     get() = _sample
 *   protected var _sample = MutableLiveData<Int>()
 * }
 */
class StateStoreSourceGenerator(
    argument: StateStoreSourceArguments
) : SourceGenerator<StateStoreSourceArguments>(argument) {

  override fun onGenerate(argument: StateStoreSourceArguments, logger: KSPLogger): TypeSpec {
    val builder = TypeSpec.classBuilder(argument.className.simpleName)
        .addKdoc(DOCUMENTATION)
        .addModifiers(KModifier.PUBLIC)
        .addModifiers(KModifier.ABSTRACT)
        .addSuperinterface(argument.superClassName)

    // create and add property
    argument.stateSpecs.forEach {
      builder.createPropertyFor(it.name, it.parameterTypeName)
    }

    return builder.build()
  }

  /**
   * @param name property name
   * @param parameterTypeName TypeName of LiveData
   */
  private fun TypeSpec.Builder.createPropertyFor(name: String, parameterTypeName: TypeName) {
    createPropertyLiveData(this, name, parameterTypeName)
    createPropertyMutableLiveData(this, name, parameterTypeName)
  }

  private fun createPropertyLiveData(
      builder: TypeSpec.Builder,
      name: String,
      parameterTypeName: TypeName
  ) {
    PropertySpec.builder(
        name,
        ClassName(LIFECYCLE_PACKAGE_NAME, LIVE_DATA_SIMPLE_NAME).parameterizedBy(parameterTypeName)
    ).addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
        .getter(FunSpec.getterBuilder().addStatement("return _$name").build())
        .build()
        .let {
          builder.addProperty(it)
        }
  }

  private fun createPropertyMutableLiveData(
      builder: TypeSpec.Builder,
      name: String,
      parameterTypeName: TypeName
  ) {
    PropertySpec.builder(
        "_$name",
        ClassName(LIFECYCLE_PACKAGE_NAME, MUTABLE_LIVE_DATA_SIMPLE_NAME).parameterizedBy(parameterTypeName)
    ).mutable()
        .addModifiers(KModifier.PROTECTED)
        .initializer("%T()", ClassName(LIFECYCLE_PACKAGE_NAME, MUTABLE_LIVE_DATA_SIMPLE_NAME).parameterizedBy(parameterTypeName))
        .build()
        .let {
          builder.addProperty(it)
        }
  }

  companion object {
    const val LIFECYCLE_PACKAGE_NAME = "androidx.lifecycle"
    const val LIVE_DATA_SIMPLE_NAME = "LiveData"
    const val MUTABLE_LIVE_DATA_SIMPLE_NAME = "MutableLiveData"
  }
}
