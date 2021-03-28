package app.junhyounglee.statestore.compiler.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

/**
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
    return builder.build()
  }

}
