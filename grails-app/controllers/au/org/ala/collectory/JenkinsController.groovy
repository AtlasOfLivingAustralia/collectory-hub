package au.org.ala.collectory

import au.org.ala.web.AlaSecured
import grails.converters.JSON

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'tempDataResource', redirectAction = 'myData')
class JenkinsController {

    CollectoryHubJenkinsService collectoryHubJenkinsService

    BootstrapJs getBsVersion() {
        BootstrapJs.valueOf(grailsApplication.config.bs.version ?: "bs2")
    }

    def console() {
        if(params.id && params.jobName){
            Map result = collectoryHubJenkinsService.consoleMessage(params.jobName, params.id, params.start)
            result.uid = params.uid
            switch (bsVersion) {
                case BootstrapJs.bs2:
                    render text: result as JSON
                    break;
                case BootstrapJs.bs3:
                    respond(result, view: 'console', model: result)
                    break;
            }
        } else {
            flash.message = 'Id parameter is mandatory'
            redirect( controller: 'tempDataResource', action: 'viewMetadata', params: [uid: params.uid])
        }
    }
}
