package properties

import properties.model.Property

class MapPropertyEndpoint(private val map: Map<String, Any?> = mutableMapOf()) : PropertyEndpoint {

    @Suppress("UNCHECKED_CAST")
    private fun getMapByKeys(keys: List<String>): Map<String, Any?>? =
        keys.fold(map as Map<String, *>) { m, k ->
            m[k]?.let {
                if (it is Map<*, *>) {
                    try {
                        it as Map<String, Any?>
                    } catch (e: Throwable) {
                        null
                    }
                } else {
                    null
                }
            } ?: return null
        }

    override suspend fun get(keys: List<String>, defaultValue: Any?): Property? =
        getMapByKeys(keys.dropLast(1))?.let {
            if (it.containsKey(keys.last())) {
                Property(it[keys.last()] ?: defaultValue)
            } else {
                null
            }
        }

    override suspend fun set(keys: List<String>, value: Any?) {
        getMapByKeys(keys.dropLast(1))?.let {
            (it as MutableMap<String, Any?>)[keys.last()] = value
        }
    }
}