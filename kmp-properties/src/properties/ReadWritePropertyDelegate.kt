package properties

import kotlin.properties.ReadWriteProperty

interface ReadWritePropertyDelegate<T>: ReadWriteProperty<Properties, T>