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
            "property" to "SubProperty"
        )
    )
)

val yamlPropertyEndpoint = YamlPropertyEndpoint("")

const val nullablePropertyDefaultValue = "Null property with default value"

class TestProperties(
    sources: List<PropertySource>,
    targets: List<PropertyTarget>,
) : Properties(
    sources = sources,
    targets = targets,
) {

    val propertyNotExists: String by property()

    var property: String by property()

    val nullableProperty: String? by property()

    val nullablePropertyWithDefault: String? by property(defaultValue = nullablePropertyDefaultValue)

    val subProperties = TestSubProperties(this, "subProperties")

}

class TestSubProperties(
    parent: Properties,
    key: String? = null,
) : Properties(parent, key) {
    val property: String by property()
}

class PropertiesTest : ExpectSpec({

    listOf(
        TestProperties(listOf(mapPropertyEndpoint), listOf(mapPropertyEndpoint)),
//        TestProperties(listOf(yamlPropertyEndpoint), listOf(yamlPropertyEndpoint)),
    ).forEach {
        context("Test Properties with sources ${it.sources.map { it::class.simpleName }} and targets ${it.targets.map { it::class.simpleName }}") {
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
                it.nullablePropertyWithDefault shouldBe nullablePropertyDefaultValue
            }

            expect("testSubProperty") {
                it.subProperties.property shouldBe it.subProperties.getValueOf<String>("property", null)
            }
        }
    }
})