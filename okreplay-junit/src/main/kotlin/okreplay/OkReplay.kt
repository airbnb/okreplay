package okreplay

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
