package properties

import com.javiersc.kotlinx.coroutines.run.blocking.runBlocking
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty

class StateFlowPropertyRegistrar<T>(
    private val key: String? = null,
    private val defaultValue: T? = null,
) {
    operator fun provideDelegate(thisRef: Properties, property: KProperty<*>): StateFlowPropertyDelegate<T> {
        val key = key ?: property.name

        return object : StateFlowPropertyDelegate<T> {
            override fun getValue(thisRef: Properties, property: KProperty<*>): StateFlow<T> =
                runBlocking { thisRef.getValueFlowOf(key, defaultValue) }
        }
    }
}