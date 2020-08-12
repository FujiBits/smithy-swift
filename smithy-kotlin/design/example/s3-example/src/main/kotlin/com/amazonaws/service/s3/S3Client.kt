/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.service.s3

import com.amazonaws.service.s3.model.*
import software.aws.clientrt.SdkClient


interface S3Client: SdkClient {
    override val serviceName: String
        get() = "s3"

    companion object {
        fun create(): S3Client = DefaultS3Client()
    }

    suspend fun putObject(input: PutObjectRequest): PutObjectResponse
    suspend fun putObject(block: PutObjectRequest.DslBuilder.() -> Unit): PutObjectResponse {
        val input = PutObjectRequest { block(this) }
        return putObject(input)
    }

    suspend fun <T> getObject(input: GetObjectRequest, block: suspend (GetObjectResponse) -> T): T

    suspend fun getBucketTagging(input: GetBucketTaggingRequest): GetBucketTaggingResponse
}