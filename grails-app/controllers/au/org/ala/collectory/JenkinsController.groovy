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
 * Created by Temi on 07/2016.
 */
package au.org.ala.collectory

import au.org.ala.web.AlaSecured
import grails.converters.JSON

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'tempDataResource', redirectAction = 'myData')
class JenkinsController {

    CollectoryHubJenkinsService collectoryHubJenkinsService

    def console() {
        if(params.id && params.jobName){
            Map result = collectoryHubJenkinsService.consoleMessage(params.jobName, params.id, params.start)
            result.uid = params.uid2
            render text: result as JSON
        } else {
            flash.message = message(code:"jenkins.missingId", default:'Id parameter is mandatory')
            redirect( controller: 'tempDataResource', action: 'viewMetadata', params: [uid: params.uid])
        }
    }
}
