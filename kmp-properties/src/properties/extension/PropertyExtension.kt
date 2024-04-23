package properties.extension

import properties.ReadWritePropertyRegistrar
import properties.StateFlowPropertyRegistrar

@Suppress("unused") // Unused suppression as it's used for extension function scoping
inline fun <reified T> property(
    key: String? = null,
    defaultValue: T? = null,
): ReadWritePropertyRegistrar<T> =
    ReadWritePropertyRegistrar(
        key,
        defaultValue,
    )

@Suppress("unused") // Unused suppression as it's used for extension function scoping
inline fun <reified T> propertyFlow(
    key: String? = null,
    defaultValue: T? = null,
): StateFlowPropertyRegistrar<T> =
    StateFlowPropertyRegistrar(
        key,
        defaultValue,
    )