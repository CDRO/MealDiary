package ch.schmidlins.mealdiary

class AnalysisEngine {
    private external fun getPatternResultNative(): String
    private external fun analyzeCorrelations(
        mealDescriptions: Array<String>,
        mealTimestamps: LongArray,
        bmTimestamps: LongArray
    ): Array<String>

    fun getPatternResult(): String {
        return if (isNativeLibraryLoaded) getPatternResultNative() else "Native engine unavailable"
    }

    fun analyze(
        meals: List<ch.schmidlins.mealdiary.data.entities.Meal>,
        bms: List<ch.schmidlins.mealdiary.data.entities.BowelMovement>
    ): List<String> {
        if (!isNativeLibraryLoaded) return emptyList()
        
        val descriptions = meals.map { it.description }.toTypedArray()
        val mTimestamps = meals.map { it.timestamp }.toLongArray()
        val bTimestamps = bms.map { it.timestamp }.toLongArray()
        
        return analyzeCorrelations(descriptions, mTimestamps, bTimestamps).toList()
    }

    companion object {
        var isNativeLibraryLoaded = false
            private set

        init {
            try {
                System.loadLibrary("mealdiary")
                isNativeLibraryLoaded = true
            } catch (e: UnsatisfiedLinkError) {
                // Ignore in tests
            }
        }
    }
}
