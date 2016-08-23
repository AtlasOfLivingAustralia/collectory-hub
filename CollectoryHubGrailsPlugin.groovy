/*
 * Copyright (C) 2016 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Created by Temi on 19/07/2016.
 */
import au.org.ala.collectory.ExtendedPluginAwareResourceBundleMessageSource
import grails.util.Environment

class CollectoryHubGrailsPlugin {
    // the plugin version
    def version = "1.1.2-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
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

        // EhCache settings
        if (!config.grails.cache.config) {
            config.grails.cache.config = {
                defaults {1
                    eternal false
                    overflowToDisk false
                    maxElementsInMemory 10000
                    timeToLiveSeconds 3600
                }

                cache {
                    name 'collectoryCache'
                    timeToLiveSeconds (3600 * 4)
                }

                cache {
                    name 'longTermCache'
                    timeToLiveSeconds (3600 * 24)
                }
            }
        }
    }
}
