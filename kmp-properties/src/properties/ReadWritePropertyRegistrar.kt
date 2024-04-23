package properties

import com.javiersc.kotlinx.coroutines.run.blocking.runBlocking
import kotlin.reflect.KProperty

class ReadWritePropertyRegistrar<T>(
    private val key: String? = null,
    private val defaultValue: T? = null,
) {
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