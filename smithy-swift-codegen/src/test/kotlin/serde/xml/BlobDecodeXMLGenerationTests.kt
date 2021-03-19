package serde.xml

import MockHttpRestXMLProtocolGenerator
import TestContext
import defaultSettings
import getFileContents
import io.kotest.matchers.string.shouldContainOnlyOnce
import org.junit.jupiter.api.Test

class BlobDecodeXMLGenerationTests {

    @Test
    fun `decode blob`() {
        val context = setupTests("Isolated/Restxml/xml-blobs.smithy", "aws.protocoltests.restxml#RestXml")
        val contents = getFileContents(context.manifest, "/example/models/XmlBlobsOutputBody+Decodable.swift")
        val expectedContents = """
        extension XmlBlobsOutputBody: Decodable {
            private enum CodingKeys: String, CodingKey {
                case data
            }
        
            public init (from decoder: Decoder) throws {
                let containerValues = try decoder.container(keyedBy: CodingKeys.self)
                let dataDecoded = try containerValues.decodeIfPresent(Data.self, forKey: .data)
                data = dataDecoded
            }
        }
        """.trimIndent()

        contents.shouldContainOnlyOnce(expectedContents)
    }

    @Test
    fun `decode blob nested`() {
        val context = setupTests("Isolated/Restxml/xml-blobs.smithy", "aws.protocoltests.restxml#RestXml")
        val contents = getFileContents(context.manifest, "/example/models/XmlBlobsNestedOutputBody+Decodable.swift")
        val expectedContents = """
        extension XmlBlobsNestedOutputBody: Decodable {
            private enum CodingKeys: String, CodingKey {
                case nestedBlobList
            }
        
            public init (from decoder: Decoder) throws {
                let containerValues = try decoder.container(keyedBy: CodingKeys.self)
                let nestedBlobListWrappedContainer = try containerValues.nestedContainer(keyedBy: WrappedListMember.CodingKeys.self, forKey: .nestedBlobList)
                let nestedBlobListContainer = try nestedBlobListWrappedContainer.decodeIfPresent([[Data]?].self, forKey: .member)
                var nestedBlobListBuffer:[[Data]?]? = nil
                if let nestedBlobListContainer = nestedBlobListContainer {
                    nestedBlobListBuffer = [[Data]?]()
                    for listContainer0 in nestedBlobListContainer {
                        var listBuffer0 = [Data]()
                        if let listContainer0 = listContainer0 {
                            for blobContainer1 in listContainer0 {
                                listBuffer0.append(blobContainer1)
                            }
                        }
                        nestedBlobListBuffer?.append(listBuffer0)
                    }
                }
                nestedBlobList = nestedBlobListBuffer
            }
        }
        """.trimIndent()

        contents.shouldContainOnlyOnce(expectedContents)
    }
    private fun setupTests(smithyFile: String, serviceShapeId: String): TestContext {
        val context = TestContext.initContextFrom(smithyFile, serviceShapeId, MockHttpRestXMLProtocolGenerator()) { model ->
            model.defaultSettings(serviceShapeId, "RestXml", "2019-12-16", "Rest Xml Protocol")
        }
        context.generator.generateDeserializers(context.generationCtx)
        context.generationCtx.delegator.flushWriters()
        return context
    }
}