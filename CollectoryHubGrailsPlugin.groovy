import au.org.ala.collectory.ExtendedPluginAwareResourceBundleMessageSource
import grails.util.Environment

class CollectoryHubGrailsPlugin {
    // the plugin version
    def version = "0.1-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Collectory Hub Plugin" // Headline display name of the plugin
    def author = "Temi Varghese"
    def authorEmail = "temi.varghese@csiro.au"
    def description = "The plugin extracts some common collectory views. This will make these pages more reusable"

    def doWithSpring = {
        def config = application.config

        // Load the "sensible defaults"
        def loadConfig = new ConfigSlurper(Environment.current.name).parse(application.classLoader.loadClass("CollectoryHubConfig"))
        application.config = loadConfig.merge(config) // client app will now override the DefaultConfig version


        // Custom message source
        messageSources(ExtendedPluginAwareResourceBundleMessageSource) {
            basenames = ["WEB-INF/grails-app/i18n/messages","${application.config.biocache.baseUrl}/facets/i18n"] as String[]
            cacheSeconds = (60 * 60 * 6) // 6 hours
            useCodeAsDefaultMessage = false
        }
    }
}
