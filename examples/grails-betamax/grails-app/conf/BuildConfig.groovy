grails.project.work.dir = "target"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
	inherits "global"
	log "warn"

	def gebVersion = "0.9.2"
	def seleniumVersion = "2.37.1"

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://oss.jfrog.org/oss-snapshot-local/"
	}
	dependencies {
		compile("org.codehaus.groovy.modules.http-builder:http-builder:0.6") {
			excludes "groovy", "xml-apis"
		}
		test "co.freeside.betamax:betamax-proxy:2.0-SNAPSHOT"
		test "co.freeside.betamax:betamax-junit:2.0-SNAPSHOT"
		test "org.gebish:geb-spock:$gebVersion"
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
