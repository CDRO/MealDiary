package ch.schmidlins.mealdiary

class AnalysisEngine {
    private external fun getPatternResultNative(): String
    private external fun isAcceleratorNative(description: String): Boolean

    fun getPatternResult(): String {
        return if (isNativeLibraryLoaded) getPatternResultNative() else "Native engine unavailable"
    }

    fun isAccelerator(description: String): Boolean {
        return if (isNativeLibraryLoaded) isAcceleratorNative(description) else false
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
