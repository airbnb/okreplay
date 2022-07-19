package okreplay

object Headers {
    const val X_OKREPLAY = "X-OkReplay"
    const val VIA_HEADER = "OkReplay"

    enum class XHeader(val headerName: String) {
        HEADER_PLAY("PLAY"),
        HEADER_REC("REC"),
    }
}
