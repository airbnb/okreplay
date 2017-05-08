package okreplay

import spock.lang.Specification
import spock.lang.Unroll
import okreplay.FilenameNormalizer

@Unroll
class FilenameNormalizerSpec extends Specification {

  void "normalized form of '#s' is '#filename'"() {
    expect:
    FilenameNormalizer.toFilename(s) == filename

    where:
    s              | filename
    "my_tape"      | "my_tape"
    "my tape"      | "my_tape"
    " my tape "    | "my_tape"
    "@my tape@"    | "my_tape"
    "my %) tape"   | "my_tape"
    "my tap\u00eb" | "my_tape"
  }
}
