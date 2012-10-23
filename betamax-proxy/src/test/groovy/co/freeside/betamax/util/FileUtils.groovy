package co.freeside.betamax.util

class FileUtils {

	private static final File TMP = new File(System.properties.'java.io.tmpdir')

	static File newTempDir(String name) {
		def dir = new File(TMP, name)
		dir.mkdirs()
		dir
	}

	private FileUtils() {}

}
