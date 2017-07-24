package okreplay

/**
 * Annotation for JUnit test classes and methods that indicates to OkReplay that network traffic
 * should be recorded/replayed according to the provided {@link TapeMode} and {@link MatchRule}s.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS,
    AnnotationTarget.FILE)
annotation class OkReplay(
    val tape: String = "",
    val mode: TapeMode = TapeMode.UNDEFINED,
    val match: Array<MatchRules> = arrayOf())
