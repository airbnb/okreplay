eventAllTestsStart = {
	def specTestTypeClass = classLoader.loadClass('grails.plugin.spock.test.GrailsSpecTestType')
	functionalTests << specTestTypeClass.newInstance('spock', 'functional')
}