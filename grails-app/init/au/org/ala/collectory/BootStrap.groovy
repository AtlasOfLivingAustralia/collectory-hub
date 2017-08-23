package au.org.ala.collectory

import grails.util.Holders

class BootStrap {

    def init = { servletContext ->
        //for backward compatibility
        if (!Holders.config.collectory.baseURL && Holders.config.collectoryUrl) {
            def url = Holders.config.collectoryUrl.toString()
            Holders.config.collectory.baseURL = url.replaceAll("/ws\$", "")
        }
    }
    def destroy = {
    }
}
