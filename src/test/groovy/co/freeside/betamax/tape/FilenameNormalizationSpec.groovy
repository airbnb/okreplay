package co.freeside.betamax.tape

import co.freeside.betamax.tape.yaml.YamlTapeLoader
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class FilenameNormalizationSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = new File(System.properties.'java.io.tmpdir', 'tapes')

	void "a tape named '#tapeName' is written to a file called '#filename'"() {
		given:
		def loader = new YamlTapeLoader(tapeRoot)
		
		expect:
		loader.fileFor(tapeName).name == filename

		where:
		tapeName       | filename
		'my_tape'      | 'my_tape.yaml'
		'my tape'      | 'my_tape.yaml'
		' my tape '    | 'my_tape.yaml'
		'@my tape@'    | 'my_tape.yaml'
		'my %) tape'   | 'my_tape.yaml'
		'my tap\u00eb' | 'my_tape.yaml'
	}
}
