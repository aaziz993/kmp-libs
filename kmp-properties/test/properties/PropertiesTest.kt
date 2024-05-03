/*        Copyright 2024 Aziz Atoev

* Licensed under the Apache License, Version 2.0 (the "License");
* You may not use this file except in compliance with the License.
* You may obtain a copy of the License at

        Apache License, Version 2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package properties

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import properties.exception.PropertyNotFoundException
import properties.extension.property

val mapPropertyEndpoint = MapPropertyEndpoint(
    mutableMapOf(
        "property" to "Property",
        "nullableProperty" to null,
        "nullablePropertyWithDefault" to null,
        "subProperties" to mutableMapOf(
            "property" to "SubProperty",
        ),
    ),
)

val yamlPropertyEndpoint = YamlPropertyEndpoint("")

const val NULLABLE_PROPERTY_DEFAULT_VALUE = "Null property with default value"

class TestProperties(sources: List<PropertySource>, targets: List<PropertyTarget>) :
    Properties(
        sources = sources,
        targets = targets,
    ) {

    val propertyNotExists: String by property()

    var property: String by property()

    val nullableProperty: String? by property()

    val nullablePropertyWithDefault: String? by property(defaultValue = NULLABLE_PROPERTY_DEFAULT_VALUE)

    val subProperties = TestSubProperties(this, "subProperties")
}

class TestSubProperties(parent: Properties, key: String? = null) : Properties(parent, key) {
    val property: String by property()
}

@Suppress("UNUSED")
class PropertiesTest :
    ExpectSpec(
        {

            listOf(
                TestProperties(listOf(mapPropertyEndpoint), listOf(mapPropertyEndpoint)),
//        TestProperties(listOf(yamlPropertyEndpoint), listOf(yamlPropertyEndpoint)),
            ).forEach {
                context(
                    "Test Properties with sources ${
                        it.sources.map {
                            it::class.simpleName
                        }
                    } and targets ${it.targets.map { it::class.simpleName }}",
                ) {
                    expect("testNotExistsProperty") {
                        shouldThrow<PropertyNotFoundException> { it.propertyNotExists }
                    }

                    expect("testProperty") {
                        it.property shouldBe it.getValueOf<String>("property", null)
                    }

                    expect("testSetProperty") {
                        it.property = "New Property value"
                        it.property shouldBe it.getValueOf<String>("property", null)
                    }

                    expect("testNullableProperty") {
                        it.nullableProperty shouldBe it.getValueOf<String>("nullableProperty", null)
                    }

                    expect("testNullablePropertyWithDefault") {
                        it.nullablePropertyWithDefault shouldBe NULLABLE_PROPERTY_DEFAULT_VALUE
                    }

                    expect("testSubProperty") {
                        it.subProperties.property shouldBe it.subProperties.getValueOf<String>("property", null)
                    }
                }
            }
        },
    )
