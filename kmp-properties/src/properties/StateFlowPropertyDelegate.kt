package properties

import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.ReadOnlyProperty

interface StateFlowPropertyDelegate<T> : ReadOnlyProperty<Properties, StateFlow<T>>