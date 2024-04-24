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

import properties.model.Property

class MapPropertyEndpoint(private val map: Map<String, Any?> = mutableMapOf()) : PropertyEndpoint {

    @Suppress("UNCHECKED_CAST")
    private fun getMapByKeys(keys: List<String>): Map<String, Any?>? = keys.fold(map as Map<String, *>) { m, k ->
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

    override suspend fun get(keys: List<String>, defaultValue: Any?): Property? = getMapByKeys(keys.dropLast(1))?.let {
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
