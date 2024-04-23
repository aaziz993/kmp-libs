package properties

import properties.model.Property
import kotlinx.coroutines.flow.StateFlow

interface PropertySource {
    suspend fun get(keys: List<String>, defaultValue: Any?): Property?

    suspend fun getFlow(keys: List<String>, defaultValue: Any?): StateFlow<*>? = null
}