package ch.schmidlins.mealdiary

class AnalysisEngine {
    external fun getPatternResult(): String
    external fun isAccelerator(description: String): Boolean

    companion object {
        init {
            System.loadLibrary("mealdiary")
        }
    }
}
