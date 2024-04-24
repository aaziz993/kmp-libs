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

import com.javiersc.kotlinx.coroutines.run.blocking.runBlocking
import kotlin.reflect.KProperty

class ReadWritePropertyRegistrar<T>(private val key: String? = null, private val defaultValue: T? = null) {
    operator fun provideDelegate(thisRef: Properties, property: KProperty<*>): ReadWritePropertyDelegate<T> {
        val key = key ?: property.name

        return object : ReadWritePropertyDelegate<T> {
            override fun getValue(thisRef: Properties, property: KProperty<*>): T =
                runBlocking { thisRef.getValueOf(key, defaultValue) }

            override fun setValue(thisRef: Properties, property: KProperty<*>, value: T) =
                runBlocking { thisRef.setValueOf(key, value) }
        }
    }
}
