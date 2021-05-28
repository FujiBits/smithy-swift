package software.amazon.smithy.swift.codegen.integration.httpResponse

import software.amazon.smithy.model.shapes.OperationShape
import software.amazon.smithy.swift.codegen.integration.ProtocolGenerator

interface HttpResponseBindingErrorGeneratable {
    fun render(ctx: ProtocolGenerator.GenerationContext, op: OperationShape)
}