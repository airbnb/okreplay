package betamax

import betamax.storage.yaml.YamlTape
import spock.lang.*

class FilenameNormalizationSpec extends Specification {

	@Unroll({"a tape named '$tapeName' is written to a file called '$filename'"})
	def "tape filenames are normalized"() {
		given:
		def tape = new YamlTape(name: tapeName)

		expect:
		tape.filename == filename

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
