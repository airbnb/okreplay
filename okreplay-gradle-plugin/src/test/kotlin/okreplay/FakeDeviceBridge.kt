package okreplay

internal class FakeDeviceBridge(private val devices: List<Device>) : DeviceBridge {
    override fun use(block: DeviceBridge.() -> Unit) = this.block()

    override fun devices(): List<Device> = devices
}