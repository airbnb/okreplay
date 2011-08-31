package betamax

import betamax.storage.yaml.YamlTapeLoader
import spock.lang.*

class FilenameNormalizationSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")

	@Unroll("a tape named '#tapeName' is written to a file called '#filename'")
	def "tape filenames are normalized"() {
		given:
		def loader = new YamlTapeLoader(tapeRoot)
		
		expect:
		loader.fileFor(tapeName).name == filename

		where:
		tapeName       | filename
		"my_tape"      | "my_tape.yaml"
		"my tape"      | "my_tape.yaml"
		" my tape "    | "my_tape.yaml"
		"@my tape@"    | "my_tape.yaml"
		"my %) tape"   | "my_tape.yaml"
		"my tap\u00eb" | "my_tape.yaml"
	}
}
