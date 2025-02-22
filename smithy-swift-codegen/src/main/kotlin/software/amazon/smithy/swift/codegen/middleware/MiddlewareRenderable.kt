/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.smithy.swift.codegen.middleware

import software.amazon.smithy.model.shapes.OperationShape
import software.amazon.smithy.swift.codegen.SwiftWriter

/**
 * Interface that allows middleware to be registered and configured with the generated protocol client
 * How this interface is used is entirely protocol/generator dependent
 */
interface MiddlewareRenderable {

    val name: String

    val middlewareStep: MiddlewareStep

    val position: MiddlewarePosition

    fun render(writer: SwiftWriter, op: OperationShape, operationStackName: String)
}
