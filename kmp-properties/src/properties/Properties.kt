/*
 * Copyright 2024 Aziz Atoev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package properties

import kotlinx.coroutines.flow.StateFlow
import properties.exception.PropertyNotFoundException
import kotlin.coroutines.cancellation.CancellationException

abstract class Properties(
    parent: Properties? = null,
    key: String? = null,
    val sources: List<PropertySource>,
    val targets: List<PropertyTarget>,
) {
    private val keys: List<String> = (parent?.keys ?: emptyList()) + (key?.let { listOf(key) } ?: emptyList())

    constructor(parent: Properties, key: String? = null) : this(parent, key, parent.sources, parent.targets)

    @Suppress("UNCHECKED_CAST")
    @Throws(PropertyNotFoundException::class, CancellationException::class)
    suspend fun <T> getValueOf(key: String, defaultValue: T?): T {
        val propertyKeys = keys + key

        sources.firstNotNullOfOrNull { it.get(propertyKeys, defaultValue) }
            ?.let { return it.value as T }

        throw PropertyNotFoundException("The property with key \"$propertyKeys\" doesn't exist.")
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(PropertyNotFoundException::class, CancellationException::class)
    suspend fun <T> getValueFlowOf(key: String, defaultValue: T?): StateFlow<T> {
        val propertyKeys = keys + key

        return sources.firstNotNullOfOrNull { it.getFlow(propertyKeys, defaultValue) }
            ?.let { it as StateFlow<T> }
            ?: throw PropertyNotFoundException("The property with keys \"$propertyKeys\" doesn't exist.")
    }

    suspend fun <T> setValueOf(key: String, value: T) {
        val propertyKeys = keys + key

        targets.forEach { it.set(propertyKeys, value) }
    }
}
