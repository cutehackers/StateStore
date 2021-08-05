package app.junhyounglee.statestore.compiler.codegen

import app.junhyounglee.statestore.compiler.kotlinpoet.toClassName
import com.google.devtools.ksp.isLocal
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import com.squareup.kotlinpoet.STAR as KpStar

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


//--
internal fun KSType.toClassName(): ClassName {
  val decl = declaration
  check(decl is KSClassDeclaration)
  return decl.toClassName()
}

internal fun KSType.toTypeName(typeParamResolver: TypeParameterResolver): TypeName {
  val type = when (val decl = declaration) {
    is KSClassDeclaration -> decl.toTypeName(arguments.map { it.toTypeName(typeParamResolver) })
    is KSTypeParameter -> typeParamResolver[decl.name.getShortName()]
    is KSTypeAlias -> decl.type.resolve().toTypeName(typeParamResolver)
    else -> error("Unsupported type: $declaration")
  }

  return type.copy(nullable = isMarkedNullable)
}

internal fun KSClassDeclaration.toTypeName(argumentList: List<TypeName> = emptyList()): TypeName {
  val className = toClassName()
  return if (argumentList.isNotEmpty()) {
    className.parameterizedBy(argumentList)
  } else {
    className
  }
}

internal interface TypeParameterResolver {
  val parametersMap: Map<String, TypeVariableName>
  operator fun get(index: String): TypeVariableName
}

internal fun List<KSTypeParameter>.toTypeParameterResolver(
    fallback: TypeParameterResolver? = null,
    sourceType: String? = null,
): TypeParameterResolver {
  val parametersMap = LinkedHashMap<String, TypeVariableName>()
  val typeParamResolver = { id: String ->
    parametersMap[id]
        ?: fallback?.get(id)
        ?: throw IllegalStateException("No type argument found for $id! Anaylzing $sourceType")
  }

  val resolver = object : TypeParameterResolver {
    override val parametersMap: Map<String, TypeVariableName> = parametersMap
    override operator fun get(index: String): TypeVariableName = typeParamResolver(index)
  }

  // Fill the parametersMap. Need to do sequentially and allow for referencing previously defined params
  for (typeVar in this) {
    // Put the simple typevar in first, then it can be referenced in the full toTypeVariable()
    // replacement later that may add bounds referencing this.
    val id = typeVar.name.getShortName()
    parametersMap[id] = TypeVariableName(id)
    // Now replace it with the full version.
    parametersMap[id] = typeVar.toTypeVariableName(resolver)
  }

  return resolver
}

internal fun KSClassDeclaration.toClassName(): ClassName {
  require(!isLocal()) {
    "Local/anonymous classes are not supported!"
  }
  val pkgName = packageName.asString()
  val typesString = qualifiedName!!.asString().removePrefix("$pkgName.")

  val simpleNames = typesString
      .split(".")
  return ClassName(pkgName, simpleNames)
}

internal fun KSTypeParameter.toTypeName(typeParamResolver: TypeParameterResolver): TypeName {
  if (variance == Variance.STAR) return KpStar
  return toTypeVariableName(typeParamResolver)
}

internal fun KSTypeParameter.toTypeVariableName(
    typeParamResolver: TypeParameterResolver,
): TypeVariableName {
  val typeVarName = name.getShortName()
  val typeVarBounds = bounds.map { it.toTypeName(typeParamResolver) }.toList()
  val typeVarVariance = when (variance) {
    Variance.COVARIANT -> KModifier.OUT
    Variance.CONTRAVARIANT -> KModifier.IN
    else -> null
  }
  return TypeVariableName.invoke(typeVarName, typeVarBounds, variance = typeVarVariance)
}

internal fun KSTypeArgument.toTypeName(typeParamResolver: TypeParameterResolver): TypeName {
  val typeName = type?.resolve()?.toTypeName(typeParamResolver) ?: return KpStar
  return when (variance) {
    Variance.COVARIANT -> WildcardTypeName.producerOf(typeName)
    Variance.CONTRAVARIANT -> WildcardTypeName.consumerOf(typeName)
    Variance.STAR -> KpStar
    Variance.INVARIANT -> typeName
  }
}

internal fun KSTypeReference.toTypeName(typeParamResolver: TypeParameterResolver): TypeName {
  val type = resolve()
  return type.toTypeName(typeParamResolver)
}
