package properties

interface PropertyTarget {
    suspend fun set(keys: List<String>, value: Any?)
}