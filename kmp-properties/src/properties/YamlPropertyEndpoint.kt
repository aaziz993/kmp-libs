package properties

import properties.model.Property

class YamlPropertyEndpoint(val fileName: String) : PropertyEndpoint {
    override suspend fun get(keys: List<String>, defaultValue: Any?): Property? {
        return null
    }

    override suspend fun set(keys: List<String>, value: Any?) {

    }


}