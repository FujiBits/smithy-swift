package software.amazon.smithy.kotlin.codegen

import java.util.function.BiFunction
import software.amazon.smithy.codegen.core.CodegenException
import software.amazon.smithy.codegen.core.Symbol
import software.amazon.smithy.codegen.core.SymbolDependency
import software.amazon.smithy.codegen.core.SymbolReference
import software.amazon.smithy.utils.CodeWriter

/**
 * Extension function that is more idiomatic Kotlin that is roughly the same purpose as
 * the provided function `openBlock(String textBeforeNewline, String textAfterNewline, Runnable r)`
 *
 * Example:
 * ```
 * writer.withBlock("{", "}") {
 *     write("foo")
 * }
 * ```
 *
 * Equivalent to:
 * ```
 * writer.openBlock("{")
 * writer.write("foo")
 * writer.closeBlock("}")
 * ```
 */
fun CodeWriter.withBlock(textBeforeNewLine: String, textAfterNewLine: String, block: CodeWriter.() -> Unit): CodeWriter {
    openBlock(textBeforeNewLine)
    block(this)
    closeBlock(textAfterNewLine)
    return this
}

class KotlinWriter(private val fullPackageName: String) : CodeWriter() {
    init {
        trimBlankLines()
        trimTrailingSpaces()
        setIndentText("    ")
        putFormatter('T', KotlinSymbolFormatter())
    }

    internal val dependencies: MutableList<SymbolDependency> = mutableListOf()
    private val imports = ImportDeclarations()

    fun addImport(symbol: Symbol, alias: String, vararg options: SymbolReference.Option) {
        // always add dependencies
        dependencies.addAll(symbol.dependencies)

        // only add imports for symbols in a different namespace
        if (!symbol.namespace.isEmpty() && symbol.namespace != fullPackageName) {
            imports.addImport(symbol.namespace, symbol.name, alias)
        }
    }

    fun addImportReferences(symbol: Symbol, vararg options: SymbolReference.ContextOption) {
        symbol.references.forEach { reference ->
            for (option in options) {
                if (reference.hasOption(option)) {
                    addImport(reference.symbol, reference.alias, *options)
                    break
                }
            }
        }
    }

    override fun toString(): String {
        val contents = super.toString()
        val header = "// Code generated by smithy-kotlin-codegen. DO NOT EDIT!\n\n"
        val importStatements = "${imports}\n\n"
        val pkgDecl = "package $fullPackageName\n\n"
        return header + pkgDecl + importStatements + contents
    }

    /**
     * Configures the writer with the appropriate opening/closing doc comment lines and calls the [block]
     * with this writer. Any calls to `write()` inside of block will be escaped appropriately.
     * On return the writer's original state is restored.
     *
     * e.g.
     * ```
     * writer.dokka(){
     *     write("This is a doc comment")
     * }
     * ```
     *
     * would output
     *
     * ```
     * /**
     *  * This is a doc comment
     *  */
     * ```
     */
    fun dokka(block: KotlinWriter.() -> Unit) {
        pushState()
        write("/**")
        setNewlinePrefix(" * ")
        block(this)
        popState()
        write(" */")
    }

    /**
     * Implements Kotlin symbol formatting for the `$T` formatter
     */
    private class KotlinSymbolFormatter : BiFunction<Any, String, String> {
        override fun apply(type: Any, indent: String): String {
            when (type) {
                is Symbol -> {
                    var formatted = type.name
                    if (type.isBoxed()) {
                        formatted += "?"
                    }

                    val defaultValue = type.defaultValue()
                    if (defaultValue != null) {
                        formatted += " = $defaultValue"
                    }
                    return formatted
                }
//                is SymbolReference -> {
//                    return type.alias
//                }
                else -> throw CodegenException("Invalid type provided for \$T. Expected a Symbol, but found `$type`")
            }
        }
    }
}
