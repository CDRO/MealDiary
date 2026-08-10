package ch.schmidlins.mealdiary

class AnalysisEngine {
    external fun getPatternResult(): String

    companion object {
        init {
            System.loadLibrary("mealdiary")
        }
    }
}
