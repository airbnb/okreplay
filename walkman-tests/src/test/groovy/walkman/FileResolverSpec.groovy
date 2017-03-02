package walkman

import com.google.common.io.Files
import spock.lang.*
import walkman.FileResolver

class FileResolverSpec extends Specification {

  @Shared @AutoCleanup("deleteDir") def baseDirectory = Files.createTempDir()
  @Subject def fileResolver = new FileResolver(baseDirectory)

  @Unroll("converts between #absolutePath and #path using base directory")
  void "converts between file and a path relative to the base directory"() {
    expect:
    fileResolver.toPath(file) == FileResolver.PATH_JOINER.join(path)

    and:
    fileResolver.toFile(*path) == file

    where:
    file                                                   | path
    new File(baseDirectory, "foo.txt")                     | ["foo.txt"]
    new File(baseDirectory, "foo${File.separator}bar.txt") | ["foo${File.separator}bar.txt"]
    new File(baseDirectory, "foo/bar/baz.txt")             | ["foo", "bar", "baz.txt"]

    absolutePath = file.absolutePath
  }

  void "does not handle files that are not in the base directory tree"() {
    when:
    fileResolver.toPath(new File(baseDirectory.parentFile, "foo.txt"))

    then:
    thrown IllegalArgumentException
  }

}
