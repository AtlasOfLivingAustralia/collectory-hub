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

import au.org.ala.ws.service.WebService
import groovy.json.JsonSlurper
import org.apache.commons.httpclient.HttpStatus

import javax.annotation.PostConstruct

class CollectoryHubJenkinsService {
    CollectoryHubService collectoryHubService
    CollectoryHubRestService collectoryHubRestServiceBean
    WebService webService
    def grailsApplication

    static final Integer MAX_TRIAL_NUMBER = 5

    @PostConstruct
    def init(){
        collectoryHubRestServiceBean = grailsApplication.mainContext.getBean("collectoryHubRestService")
    }

    /**
     * Get an optional crumb from the jenkins server for authentication.
     *
     * @return A map of headers to add to the jenkins request
     */
    protected Map getCrumb() {
        def headers = [:]
        if (grailsApplication.config.jenkins.useCrumb?.toBoolean()) {
            String url = "${grailsApplication.config.jenkins.url}/crumbIssuer/api/json"
            def cr = collectoryHubService.doGet(url)
            if (cr.status != HttpStatus.SC_OK) {
                log.error("Unable to get crumb for jenkins job from ${url}, response=${cr}")
                throw new Exception("Unable to get crumb for jenkins job")
            }
            def resp = new JsonSlurper().parseText(cr.resp)
            def crumb = resp.crumb
            def header = resp.crumbRequestField
            if (crumb && header)
                headers[header] = crumb
            else {
                log.warn("Unable to get crumb/header from ${url}, response ${cr}")
                throw new Exception("Unable to get crumb for jenkins job")
            }
        }
        return headers
    }

    /**
     * Submit a parameterized job to jenkins.
     * @param jobName - parameterized job name
     * @param uid - drt number
     * @param dr - dr number
     * @return
     */
    public Map submitParameterizedJob(String jobName, String uid, String dr){
        def headers = getCrumb()
        // Jenkins doesn't like + encoded spaces in URLs
        jobName = URLEncoder.encode(jobName, "UTF-8").replaceAll('\\+', '%20')
        String url = "${grailsApplication.config.jenkins.url}/job/${jobName}/buildWithParameters?" +
                "token=${grailsApplication.config.jenkins.token}&apikey=${grailsApplication.config.webservice.apiKey}" +
                "&uid=${uid}&dr=${dr}&collectory=${grailsApplication.config.collectory.baseURL}"
        collectoryHubService.doPost(url,"", headers)
    }

    /**
     * Run a jenkins job that does all steps including indexing.
     * @param uid - drt number
     * @param dr - dr number
     * @return
     */
    public Map processWithIndexing(String uid, String dr){
        String jobName = grailsApplication.config.jenkins.processWithIndexing
        if(jobName){
            return submitParameterizedJob(jobName, uid, dr)
        } else {
            log.error("Config jenkins.processWithIndexing missing.")
            throw new Exception("Jenkins job name not provided in config.")
        }
    }

    /**
     * Run a jenkins job that does all steps excluding indexing.
     * @param uid - drt number
     * @param dr - dr number
     * @return
     */
    public Map processWithoutIndexing(String uid, String dr){
        String jobName = grailsApplication.config.jenkins.processWithoutIndexing
        if(jobName){
            return submitParameterizedJob(jobName, uid, dr)
        } else {
            log.error("Config jenkins.processWithoutIndexing missing.")
            throw new Exception("Jenkins job name not provided in config.")
        }
    }

    /**
     * Progressive console text lookup on Jenkins
     * @param jobName
     * @param buildNumber
     * @param start
     * @return
     */
    public Map consoleMessage(String jobName, String buildNumber, String start){
        def headers = getCrumb()
        // Jenkins doesn't like + encoded spaces in URLs
        encJobName = URLEncoder.encode(jobName, "UTF-8").replaceAll('\\+', '%20')
        String url = "${grailsApplication.config.jenkins.url}/job/${encJobName}/${buildNumber}/logText/progressiveText?start=${start}"
        Map result = collectoryHubService.doGet(url, headers)
        Map msg = [
                jobName: jobName,
                buildNumber: buildNumber,
                start: start,
                isMoreData : false
        ]
        msg.text = result.resp
        for(int i = 0; i < result.headers?.size(); i++){
            switch (result.headers[i].getName()){
                case 'X-More-Data':
                    if(result.headers[i].getValue()){
                        msg.isMoreData = result.headers[i].getValue()
                    }
                    break;
                case 'X-Text-Size':
                    msg.nextStart = result.headers[i].getValue()
                    break;
            }
        }

        msg
    }

    /**
     * Test run a production ready dataset
     * @param uid - drt id
     */
    Map testRun(String uid){
        Map drt = collectoryHubRestServiceBean.getTempDataResource(uid)
        Map result = [:]
        result.jobName = grailsApplication.config.jenkins.testRun
        if(result.jobName){
            Map jenkinsJob = submitParameterizedJob(result.jobName, '', drt.prodUid)
            if(jenkinsJob.status in [200, 201]){
                result.buildNumber = getBuildNumberFromQueue(jenkinsJob.location)
                result.start = 0
            } else {
                throw new Exception("Error while retrieving build number.")
            }
        } else {
            log.error("Config jenkins.processWithoutIndexing missing.")
            throw new Exception("Jenkins job name not provided in config.")
        }

        result
    }

    /**
     * Get build number from queue url
     * @param queueUrl
     * @return
     */
    Integer getBuildNumberFromQueue(String queueUrl, Integer retryNumber = 0){
        if(retryNumber < MAX_TRIAL_NUMBER){
            String url = "${queueUrl}/api/json"
            Map result = webService.get(url)
            if(result.statusCode in [200, 201]){
                Map json = result.resp
                if(!json?.executable){
                    sleep(3000)
                    return getBuildNumberFromQueue(queueUrl, retryNumber + 1)
                } else {
                    return json.executable.number
                }
            } else {
                throw new Exception("Error while retrieving build number.")
            }
        }
    }
}
