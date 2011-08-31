grails.servlet.version = "2.5"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "error"
    checksums true

    repositories {
        inherits true
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
		mavenRepo "http://oss.sonatype.org/content/groups/public/"
    }
    dependencies {
		compile("org.codehaus.groovy.modules.http-builder:http-builder:0.5.1") {
			excludes "groovy", "xml-apis"
		}
		test "com.github.robfletcher:betamax:1.0-M1"
    }

    plugins {
        compile ":hibernate:$grailsVersion"
        compile ":jquery:1.6.1.1"
        compile ":resources:1.0.2"

        build ":tomcat:$grailsVersion"

		test "org.spockframework:spock:0.6-groovy-1.8-SNAPSHOT"
    }
}
