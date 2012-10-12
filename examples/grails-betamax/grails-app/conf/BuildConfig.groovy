grails.project.work.dir = "target"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
	inherits "global"
	log "warn"

	def gebVersion = "0.7.2"
	def seleniumVersion = "2.25.0"

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://oss.sonatype.org/content/groups/public/"
	}
	dependencies {
		compile("org.codehaus.groovy.modules.http-builder:http-builder:0.5.1") {
			excludes "groovy", "xml-apis"
		}
		test 'co.freeside:betamax:1.1.1'
		test "org.codehaus.geb:geb-spock:$gebVersion"
		test("org.seleniumhq.selenium:selenium-htmlunit-driver:$seleniumVersion") {
			exclude "xml-apis"
		}
	}
	plugins {
		compile ":hibernate:$grailsVersion"
		build ":tomcat:$grailsVersion"
		test ":spock:0.6"
		test ":geb:$gebVersion"
	}
}
