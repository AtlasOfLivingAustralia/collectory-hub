/*
 * Copyright (c) 2015 Atlas of Living Australia
 * All Rights Reserved.
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.collectory

import au.org.ala.web.AuthService
import au.org.ala.web.UserDetails
import au.org.ala.ws.service.WebService
import grails.converters.JSON
import grails.plugin.cache.Cacheable
import groovy.json.JsonSlurper
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.util.URIUtil
import org.springframework.context.MessageSource

import javax.annotation.PostConstruct
import java.text.SimpleDateFormat

class CollectoryHubRestService {
    def grailsApplication
    WebService webService
    AuthService authService
    MessageSource messageSource

    static final String DEFAULT_API_KEY_HEADER = "api_key"
    static final String USER_HEADER = "user"
    static final List  REQUIRED_FIELDS = ["name", "description", "license", "citation"]
    static final List COMMON_FIELDS_DR_DRT = ['informationWithheld', 'dataGeneralizations', 'citation', 'name', '']
    String COLLECTIONS_BASE_URL

    @PostConstruct
    init(){
        COLLECTIONS_BASE_URL = grailsApplication.config.collections.baseUrl
    }

    /**
     * get all data resource id details satisfying a query condition.
     * @param context - all, hub - all corresponds to all data in ALA and hub for a particular hub config eg. MDBA hub
     * @return
     */
    @Cacheable('longTermCache')
    List getDataResources(String context){
        String url;
        List drDetails = []
        switch (context){
            case 'all':
                url = "${grailsApplication.config.biocache.baseUrl}/occurrences/search?q=${URIUtil.encodeWithinQuery(grailsApplication.config.biocache.queryContext)}&facets=data_resource_uid&flimit=1000000"
                break;
            case 'hub':
                url = "${grailsApplication.config.biocache.baseUrl}/occurrences/search?q=${URIUtil.encodeWithinQuery(grailsApplication.config.hub.queryContext)}&facets=data_resource_uid&flimit=1000000"
                break;
        }


        Map results = webService.get(url)?.resp?:[:]
        // get data resource ids
        List drs = getDataResourceIdFromResult(results)
        // get data resource id details from collectory
        List dataResourceDetails = getDataResourcesFromCollectory(grailsApplication.config.collectory.resources)


        drs.each { dr ->
            Map dataResourceMetadata = dataResourceDetails.find {
                it.uid == dr
            }

            if (dataResourceMetadata) {
                drDetails.push(dataResourceMetadata)
            }
        }

        drDetails
    }

    /**
     * get detials of all data resource ids in collectory
     * @param url
     * @return
     */
    @Cacheable('longTermCache')
    List getDataResourcesFromCollectory(String url){
        webService.get(url)?.resp?:[]
    }

    /**
     * Query data resource name and return a list of matching data resource ids
     * @param query
     * @param source
     * @return ['dr1','dr2']
     */
    List getFilteredDataResources(String query, String source){
        List drs = getDataResources(source) ?:[]
        List filteredDrs = drs.findAll {
            it.name?.toLowerCase().contains(query)
        }

        filteredDrs?.collect{
            it.uid
        }
    }

    /**
     * get data resource ids from facet result
     * @param results
     * @return
     */
    private List getDataResourceIdFromResult(Map results){
        List dataResources = []
        if(results?.facetResults?.size()){
            List drs = results?.facetResults[0]?.fieldResult
            if(drs?.size()){
                dataResources = drs.collect { dr ->
                    dr.fq?.replaceAll('"','').replace('data_resource_uid:','')
                }
            }
        }

        dataResources
    }

    /**
     * get data resource id details and contact person details
     * @param id - data resource id
     * @return
     */
    Map getDataResource(String id){
        Map resource
        Map resultResource =  webService.get("${COLLECTIONS_BASE_URL}/ws/dataResource/${id}")
        if(!resultResource.error){
            resource = resultResource.resp
            String contractsUrl = "${COLLECTIONS_BASE_URL}/ws/dataResource/${id}/contact.json"
            Map result = webService.get(contractsUrl)
            List contacts = result?.resp?:[]
            resource.put('contacts',contacts)
        }

        resource
    }

    /**
     * get institution details and contact person details.
     * @param id
     * @return
     */
    Map getInstitution(String id){
        Map resource
        Map result = webService.get("${COLLECTIONS_BASE_URL}/ws/institution/${id}")
        if(!result.error){
            resource =  result.resp;
            String contractsUrl = "${COLLECTIONS_BASE_URL}/ws/institution/${id}/contact.json"
            Map resultContacts = webService.get(contractsUrl)
            List contacts = resultContacts?.resp?:[];
            resource.put('contacts',contacts)
        }

        resource
    }


    /**
     * Retrieves a listing of uploads for this user.
     * @return JSON list of data resources
     */
    def getUserUploads(String currentUserId){
        try {
            def url = "${grailsApplication.config.collectoryUrl}/tempDataResource?alaId=${currentUserId}"
            def js = new JsonSlurper()
            js.parseText(new URL(url).text)
        } catch (Exception e){
            log.error(e.getMessage(), e)
            null
        }
    }

    /**
     * Get metadata of a temp data resource and append system metadata to it
     * @param uid - drt1
     * @return {@link Map}
     */
    Map getTempDataResource(String uid){
        Map tempMeta = getTempDataResourceFromCollectory(uid)
        if(tempMeta && tempMeta.size()){
            tempMeta = cleanTempDateResource(tempMeta)
            preFillSystemDetails(tempMeta)
        } else {
            return null
        }
    }

    /**
     * Get metadata for a temp data resource from collectory
     * @param uid
     * @return
     */
    public Map getTempDataResourceFromCollectory(String uid) {
        String url = "${grailsApplication.config.collectoryUrl}/tempDataResource?uid=${uid}"
        Map result = webService.get(url)
        result.resp ?: [:]
    }

    /**
     * Pre-fill temp metadata with user name, links to resources etc
     * @param tempMeta
     * @return
     */
     Map preFillSystemDetails(Map tempMeta){
        String userId = tempMeta.alaId
        if(userId){
            UserDetails user = authService.getUserForUserId(userId, true)
            tempMeta.displayName = user.displayName?:user.userName?:user.userId
            tempMeta.sourceFile = ""
        }

        // only add these links if plugin is used in sandbox environment.
        if(grailsApplication.config.sandboxHubsWebapp){
            tempMeta.sandboxLink = "${grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:${tempMeta.uid}"
            tempMeta.sourceFileUrl = "${grailsApplication.config.sandboxHubsWebapp}/api/dataCheck/serveFile?uid=${tempMeta.uid}"
            tempMeta.reloadLink = "${grailsApplication.config.grails.serverURL}/upload/reload?dataResourceUid=${tempMeta.uid}"
            tempMeta.deleteLink = "${grailsApplication.config.grails.serverURL}/myDatasets/deleteResource?uid=${tempMeta.uid}"
            if(tempMeta.prodUid){
                tempMeta.collectoryLink = "${grailsApplication.config.collectory.baseUrl}/public/show/${tempMeta.prodUid}"
            }
        }

        tempMeta
    }

    /**
     * convert a timestamp to human recognizable format
     * @param timestamp
     * @return
     */
    String getDateAndTimeFromTimeStamp(String timestamp){
        Date date = new Date().parse("yyyy-MM-dd'T'HH:mm:ss", timestamp)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.format(date);
    }

    Map cleanTempDateResource(Map tempMeta){
        tempMeta.dateCreated = getDateAndTimeFromTimeStamp( tempMeta.dateCreated )
        tempMeta.lastUpdated = getDateAndTimeFromTimeStamp( tempMeta.lastUpdated )
        tempMeta.status = tempMeta.status?:'draft'
        tempMeta
    }

    /**
     * Save temp data resource metadata on collectory.
     * @params uid - required - temp data resource id
     * @params All temp data resource related properties - check au.org.ala.collectory.TempDataResource in Collectory Plugin.
     */
    Map saveTempDateResource(Map data){
        if(data.uid){
            String url = "${grailsApplication.config.collectoryUrl}/tempDataResource/${data.uid}"
            data[DEFAULT_API_KEY_HEADER] = grailsApplication.config.webservice.apiKey
            data.remove('controller')
            data.remove('action')
            String body = (data as JSON).toString()
            doPost(url, body)
        }
    }

    Map doPost(String url, String body){
        PostMethod post = new PostMethod(url);
        post.setRequestHeader("Authorization", grailsApplication.config.webservice.apiKey);
        post.setRequestBody(body);
        HttpClient httpClient = new HttpClient();
        int statusCode = httpClient.executeMethod(post);
        [status: statusCode, location: post.getResponseHeader('location')?.value]
    }

    /**
     * Get all temp data resource for a user.
     * @param userId - ala id
     * @return List of temp data resources
     */
    List getListOfTempDataResource(String userId){
        def userUploads = getUserUploads(userId)
        //filter uploads by biocache URL
        def filteredUploads = userUploads.findAll { upload ->
            upload.webserviceUrl == grailsApplication.config.biocacheServiceUrl
        }

        filteredUploads.each { dtr ->
            cleanTempDateResource(dtr)
        }
    }

    /**
     *
     * @param uid
     * @return
     */
    Map submitTempDataResourceForReview(String uid){
        Map drt = getTempDataResourceFromCollectory(uid)
        Map result = checkIfMandatoryFieldsAreFilled(drt)
        Map saveResult
        if(result?.isValid){
            saveResult = saveTempDateResource([status: 'submitted', uid: uid])
            saveResult.isValid = true
            return saveResult
        }

        result
    }

    /**
     * Checks if required fields are filled by owner of temp data resource.
     * It returns appropriate message according to fields failing the mandatory condition
     * @param drt - {@link Map} - temp data resource fields
     * @return
     */
    Map checkIfMandatoryFieldsAreFilled(Map drt){
        Boolean valid  = true
        List inValidFields = []
        REQUIRED_FIELDS.each{ field ->
            if(drt[field] != null && drt[field] != ""){
                valid = valid && true
            } else {
                valid = valid && false
                inValidFields.push(field)
//                inValidFields.push(messageSource.getMessage("tempDataResource.status.${field}", [], null))
            }
        }

        [ isValid: valid, invalidFields: inValidFields, message: "The following fields are mandatory - ${inValidFields.join(', ')}." ]
    }

    /**
     * Decline a data resource. This function will update status of data resource.
     * @param uid - drt id
     * @return
     */
    Map declineTempDataResource(String uid){
        saveTempDateResource([status: 'declined', uid: uid])
    }

    /**
     * Set status of a data resource to draft.
     * @param uid - drt id
     * @return
     */
    Map draftTempDataResource(String uid){
        Map drt = getTempDataResourceFromCollectory(uid)
        if(drt.status in ['declined', 'dataAvailable']) {
            saveTempDateResource([status: 'draft', uid: uid])
        } else {
            return [status: 400, message: 'Only declined or data available data resource can be drafted.']
        }
    }

    /**
     * Load a temp data resource to production.
     * @param uid
     * @return
     */
    Map loadToProduction(String uid){
        // todo: check if data file is present
        String drId
        Map drt = getTempDataResourceFromCollectory(uid)
        Map dr = convertTempDataResourceToDataResource(drt)

        Map result = createOrSaveDataResource(drt.prodUid, dr)
        if(result.uid){
            Map updatingTempDR = saveTempDateResource([prodUid: result.uid, uid: drt.uid])
            if(!(updatingTempDR.status in [200, 201])){
                // todo: delete data resource
                throw new Exception("Error loading data to production. Error occurred while updating temp data resource.")
            }
        }

        // todo : create jenkins job
        result
    }

    /**
     * Create or update a data resource. If uid is passed, then function will update data resource. Otherwise, create
     * a new data resource
     * @param uid - dr id
     * @param dr - data resource fields
     * @return [ uid: dr1, status: 200, location: '/ws/dataResource/dr1' ]
     */
    public Map createOrSaveDataResource(String uid, Map dr) {
        String url, drId
        if (!uid) {
            url = "${grailsApplication.config.collectoryUrl}/dataResource"
        } else {
            url = "${grailsApplication.config.collectoryUrl}/dataResource/${uid}"
        }

        dr[DEFAULT_API_KEY_HEADER] = grailsApplication.config.webservice.apiKey
        dr[USER_HEADER] = authService.getEmail()
        String body = (dr as JSON).toString()
        Map result = doPost(url, body)
        if (result.status in [200, 201]) {
            if(!uid){
                drId = parseUidFromLocation(result.location)
                result.uid = drId
            }
        } else {
            throw new Exception("Error loading data to production. Error occurred while creating data resource in collectory.")
        }

        result
    }

    private String parseUidFromLocation(String location, String resourceName = 'dataResource'){
        if(location){
            String[] paths = location.split("/${resourceName}/")
            return paths[paths.length - 1]
        }
    }

    /**
     * Map temp data resource properties to data resource properties
     * @param drt
     * @return
     */
    Map convertTempDataResourceToDataResource(Map drt){
        Map dr = [:]
        COMMON_FIELDS_DR_DRT.each{ field ->
            dr[field] = drt[field]
        }

        dr.pubDescripiton = drt.description
        // source file
        // todo: replace key fields
        dr.connectionParameters = (getConnectionParameters(drt.sourceFile, ['catalogNumber']) as JSON).toString()
        dr
    }

    /**
     * Create connection parameters to help parse the csv file
     * @param url - location of file
     * @param keyFields - List - fields combined to create a unique identifier for records in this resource
     * @return
     */
    private Map getConnectionParameters(String url, List keyFields){
        [
                "protocol": "DwC",
                "csv_text_enclosure": "\"",
                "termsForUniqueKey": keyFields,
                "csv_eol": "",
                "csv_delimiter": ",",
                "automation": false,
                "incremental": false,
                "strip": false,
                "csv_escape_char": "\\",
                "url": url
        ]
    }
}
