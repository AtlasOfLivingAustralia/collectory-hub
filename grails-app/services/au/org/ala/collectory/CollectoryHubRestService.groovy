/*
 * Copyright (c) 2016 Atlas of Living Australia
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
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import org.apache.commons.httpclient.util.URIUtil

import javax.annotation.PostConstruct
import java.text.SimpleDateFormat

/**
 * Helper funcitons to interacting with collectory for entities like temp data resource and data resource.
 */
class CollectoryHubRestService {
    def grailsApplication
    WebService webService
    AuthService authService
    CollectoryHubJenkinsService collectoryHubJenkinsService
    CollectoryHubService collectoryHubService

    static final String DEFAULT_API_KEY_HEADER = "api_key"
    static final String USER_HEADER = "user"
    static final List  REQUIRED_FIELDS = ["name", "description", "license", "citation"]
    static final List COMMON_FIELDS_DR_DRT = ['informationWithheld', 'dataGeneralizations', 'citation', 'name']
    static final List TEMP_DATA_RESROUCE_STATUSES = ['draft', 'submitted', 'declined', 'queuedForLoading', 'dataAvailable']
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

        Map results
        Map result = webService.get(url)
        if(result.statusCode in [200, 201]){
            results = result.resp?:[:]
        } else {
            throw new Exception("Could not get temp data resource for user ${currentUserId}")
        }

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
        Map result = webService.get(url)
        if(result.statusCode in [200, 201]){
            result.resp
        } else {
            throw new Exception("Could not get temp data resource for user ${currentUserId}")
        }
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
        Map resultResource =  webService.get("${COLLECTIONS_BASE_URL}/ws/dataResource/${id}")
        if(!resultResource.error){
            Map resource
            resource = resultResource.resp
            String contractsUrl = "${COLLECTIONS_BASE_URL}/ws/dataResource/${id}/contact.json"
            Map result = webService.get(contractsUrl)
            List contacts = result?.resp?:[]
            resource.put('contacts',contacts)
            resource
        } else {
            throw new Exception(resultResource.error)
        }
    }

    /**
     * get institution details and contact person details.
     * @param id
     * @return
     */
    Map getInstitution(String id){
        Map result = webService.get("${COLLECTIONS_BASE_URL}/ws/institution/${id}")
        if(!result.error){
            Map resource
            resource =  result.resp;
            String contractsUrl = "${COLLECTIONS_BASE_URL}/ws/institution/${id}/contact.json"
            Map resultContacts = webService.get(contractsUrl)
            List contacts = resultContacts?.resp?:[];
            resource.put('contacts',contacts)
            resource
        } else {
            throw new Exception(result.error)
        }

    }


    /**
     * Retrieves a listing of uploads for this user.
     * @return JSON list of data resources
     */
    Map getUserUploads(String currentUserId, String webserviceUrl = '', String max =10, String offset=0,  String status='',
                       String sortField='lastUpdated', String sortOrder='desc'){
        def url = "${grailsApplication.config.collectoryUrl}/tempDataResource?alaId=${currentUserId}" +
                "&webserviceUrl=${URIUtil.encodeWithinQuery(webserviceUrl)}&max=${max}&offset=${offset}&status=${status}&sortField=${sortField}" +
                "&sortOrder=${sortOrder}"
        Map result = webService.get(url)
        if(result.statusCode in [200, 201]){
            result.resp
        } else {
            throw new Exception("Could not get temp data resource for user ${currentUserId}")
        }
    }

    /**
     * Get metadata of a temp data resource and append system metadata to it
     * @param uid - drt1
     * @return {@link Map}
     */
    @Cacheable(value = "collectoryCache", key = "#uid")
    Map getTempDataResource(String uid){
        Map tempMeta = grailsApplication.mainContext.collectoryHubRestService.getTempDataResourceFromCollectory(uid)
        if(tempMeta && tempMeta.size()){
            tempMeta = cleanTempDateResource(tempMeta)
            preFillSystemDetails(tempMeta)
        } else {
            throw new Exception("Could not find temp data resource ${uid}")
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
        if(result.statusCode in [200, 201]){
            result.resp
        } else {
            throw new Exception("Could not get temp data resource ${uid}")
        }
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
            tempMeta.displayName = user?.displayName?:user?.userName?:user?.userId
            tempMeta.sourceFile = ""
        }

        // only add these links if plugin is used in sandbox environment.
        if(grailsApplication.config.sandboxHubsWebapp){
            tempMeta.sandboxLink = "${grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:${tempMeta.uid}"
            tempMeta.sourceFileUrl = "${grailsApplication.config.grails.serverURL}/dataCheck/serveFile?uid=${tempMeta.uid}"
            tempMeta.reloadLink = "${grailsApplication.config.grails.serverURL}/tempDataResource/reload?uid=${tempMeta.uid}"
            tempMeta.deleteLink = "${grailsApplication.config.grails.serverURL}/datasets/deleteResource?uid=${tempMeta.uid}"
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

    /**
     * Make properties more human friendly
     * @param tempMeta - drt map
     * @return
     */
    Map cleanTempDateResource(Map tempMeta){
        tempMeta.dateCreated = getDateAndTimeFromTimeStamp( tempMeta.dateCreated )
        tempMeta.lastUpdated = getDateAndTimeFromTimeStamp( tempMeta.lastUpdated )
        tempMeta
    }

    /**
     * Save temp data resource metadata on collectory.
     * @params uid - required - temp data resource id
     * @params All temp data resource related properties - check au.org.ala.collectory.TempDataResource in Collectory Plugin.
     */
    @CacheEvict(value="collectoryCache", key= "#uid", beforeInvocation = true)
    Map saveTempDataResource(Map data, String uid){
        if(uid){
            String url = "${grailsApplication.config.collectoryUrl}/tempDataResource/${uid}"
            data[DEFAULT_API_KEY_HEADER] = grailsApplication.config.webservice.apiKey
            data.sourceFile = "${grailsApplication.config.grails.serverURL}/dataCheck/serveFile?uid=${uid}"
            data.remove('controller')
            data.remove('action')
            String body = (data as JSON).toString()
            collectoryHubService.doPost(url, body)
        }
    }

    /**
     * Get all temp data resource for a user.
     * @param userId - ala id
     * @param max
     * @param offset
     * @param status
     * @param sortField
     * @param sortOrder
     * @return A map containing list of temp data resources and total temp data resources in db matiching passed criteria.
     */
    Map getListOfTempDataResource(String userId, String max =10, String offset=0,  String status='',
        String sortField='lastUpdated', String sortOrder='desc') {
        Map userUploads = getUserUploads(userId, grailsApplication.config.biocacheServiceUrl, max, offset,  status,
                sortField, sortOrder)

        userUploads.resources = userUploads.resources?.collect { dtr ->
            cleanTempDateResource(dtr)
        }

        userUploads
    }

    /**
     * Submit a user uploaded dataset for review.
     * @param uid
     * @return
     */
    Map submitTempDataResourceForReview(String uid){
        Map drt = grailsApplication.mainContext.collectoryHubRestService.getTempDataResource(uid)
        Map result = checkIfMandatoryFieldsAreFilled(drt)
        Map saveResult
        if(result?.isValid){
            // update contact details
            Integer contactId = getOrCreateContact(authService.getEmail(), authService.userId)
            createOrUpdateContactForEntity('tempDataResource', uid, contactId)

            saveResult = grailsApplication.mainContext.collectoryHubRestService.saveTempDataResource([status: 'submitted'], uid)
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

        [ isValid: valid, invalidFields: inValidFields, message: "The following fields are mandatory - ${inValidFields.join(', ')}.", args: [inValidFields.join(', ')] ]
    }

    /**
     * Decline a data resource. This function will update status of data resource.
     * @param uid - drt id
     * @return
     */
    Map declineTempDataResource(String uid){
        grailsApplication.mainContext.collectoryHubRestService.saveTempDataResource([status: 'declined'], uid)
    }

    /**
     * Set status of a data resource to draft.
     * @param uid - drt id
     * @return
     */
    Map draftTempDataResource(String uid){
        Map drt = grailsApplication.mainContext.collectoryHubRestService.getTempDataResource(uid)
        if(drt.status in ['declined', 'dataAvailable', 'queuedForLoading']) {
            grailsApplication.mainContext.collectoryHubRestService.saveTempDataResource([status: 'draft'], uid)
        } else {
            return [status: 400, message: 'Only declined or data available data resource can be drafted.']
        }
    }

    /**
     * Load a temp data resource to production.
     * @param uid
     * @return
     */
    Map loadToProduction(String uid, Boolean indexing){
        if(isDataSubmittedForTempDataResource(uid)){
            def (String drId, Map drt) = createOrSaveDataResource(uid)

            // update contact details
            Integer contactId = getOrCreateContact(authService.getEmail(), authService.userId)
            createOrUpdateContactForEntity('dataResource', uid, contactId)

            Map indexResult
            Map result =[:]
            if(indexing){
                indexResult = collectoryHubJenkinsService.processWithIndexing(uid, drId)
                result.jobName = grailsApplication.config.jenkins.processWithIndexing
            } else {
                indexResult = collectoryHubJenkinsService.processWithoutIndexing(uid, drId)
                result.jobName = grailsApplication.config.jenkins.processWithoutIndexing
            }

            if(indexResult.status in [200,201]){
                Map jenkinsResult = grailsApplication.mainContext.collectoryHubRestService.saveTempDataResource([status: 'queuedForLoading'], uid)
                if(!(jenkinsResult.status in [200,201])){
                    throw new Exception("Error loading data to production. Error occurred while updating temp data resource.")
                }
            }

            if(indexResult?.location){
                result.buildNumber = collectoryHubJenkinsService.getBuildNumberFromQueue(indexResult.location)
                result.start = 0
            }

            result
        } else {
            throw new Exception("Could not load to production. No data loaded.")
        }
    }

    /**
     * Create or update a data resource for a uid
     * @param uid - drt id
     * @return
     */
    public List createOrSaveDataResource(String uid) {
        if(isDataSubmittedForTempDataResource(uid)) {
            Map drt = grailsApplication.mainContext.collectoryHubRestService.getTempDataResource(uid)
            Map dr = convertTempDataResourceToDataResource(drt)

            Map result = createOrSaveDataResource(drt.prodUid, dr)
            String newDrId = result.uid, drId = newDrId?:drt.prodUid
            if(!(result.status in [200, 201])){
                throw new Exception("An error occurred while creating new data resource.")
            } else {
                if(newDrId){
                    Map updatingTempDR = grailsApplication.mainContext.collectoryHubRestService.saveTempDataResource([prodUid: newDrId], uid)
                    if(!(updatingTempDR.status in [200, 201])){
                        // roll back created data resource
                        deleteDataResource(newDrId)
                        throw new Exception("Error saving data resource uid to collectory. Error occurred while updating temp data resource.")
                    }
                }
            }

            [drId, drt]
        } else {
            [:]
        }
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
        Map result = collectoryHubService.doPost(url, body)
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

        dr.makeContactPublic = drt.isContactPublic
        dr.pubDescripiton = drt.description
        addLicenseAndVersion(dr, drt.license)
        // source file
        String[] fields = drt.keyFields?.split(',')
        String separator = drt.csvSeparator?:','
        dr.connectionParameters = (getConnectionParameters(drt.sourceFileUrl, fields, separator) as JSON).toString()
        dr
    }

    /**
     * map license from temp data resource to license in data resource
     * @param dr
     * @param license
     * @return
     */
    Map addLicenseAndVersion(Map dr, String license){
        switch (license){
            case 'CCBY3Aus':
                dr.licenseType = 'CC BY'
                dr.licenseVersion = "3.0"
                break;
            case 'CCBYNC3Aus':
                dr.licenseType = 'CC BY-NC-Aus'
                dr.licenseVersion = "3.0"
                break;
            case 'CCBY4Int':
                dr.licenseType = 'CC BY-Int'
                dr.licenseVersion = "4.0"
                break;
            case 'CCBYNC4Int':
                dr.licenseType = 'CC BY-NC-Int'
                dr.licenseVersion = "4.0"
                break;
            case 'CC0':
                dr.licenseType = 'CC0'
                break;
        }

        dr
    }

    /**
     * Create connection parameters to help parse the csv file
     * @param url - location of file
     * @param keyFields - List - fields combined to create a unique identifier for records in this resource
     * @return
     */
    private Map getConnectionParameters(String url, String[] keyFields, String separator = ','){
        [
                "protocol": "DwC",
                "csv_text_enclosure": "\"",
                "termsForUniqueKey": keyFields,
                "csv_eol": "",
                "csv_delimiter": separator,
                "automation": false,
                "incremental": false,
                "strip": false,
                "csv_escape_char": "\\",
                "url": url
        ]
    }

    /**
     * check if data file is present for a given temp data resource
     * @param uid - drt id
     * @return
     */
    Boolean isDataSubmittedForTempDataResource(String uid){
        String url = "${grailsApplication.config.grails.serverURL}/dataCheck/serveFile?uid=${uid}"
        Map result = collectoryHubService.doHead(url)
        return  result.status in [200, 201]
    }

    /**
     * delete a data resource
     * @param uid
     * @return
     */
    public Map deleteDataResource(String uid){
        deleteAnEntity('dataResource', uid)
    }

    /**
     * delete a data resource
     * @param uid
     * @return
     */
    public Map deleteTempDataResource(String uid){
        deleteAnEntity('tempDataResource', uid)
    }

    /**
     * An function which will delete an entity on collectory
     * @param entity
     * @param uid
     */
    public void deleteAnEntity(String entity, String uid) {
        String url = "${grailsApplication.config.collectoryUrl}/${entity}/${uid}"
        int statusCode = collectoryHubService.doDelete(url)
        [status: statusCode]
    }

    /**
     * Map details of contact from userdetails system to collectory compatible format.
     * @param alaId
     * @return
     */
    Map createContactMap(String alaId){
        UserDetails userDetails = authService.getUserForUserId(alaId)
        Map contact = [:]
        if(userDetails){
            String[] names = userDetails.displayName?.split(" ")
            if(names?.size()>0){
                contact.firstName = names[0]
                if(names?.size()>1){
                    contact.lastName = names[names.size() -1 ]
                }
            }

            contact.email = userDetails.userName
            contact.phone = userDetails.telephone
            contact.publish = false
        }

        contact
    }

    /**
     * get a user's contact id. If user does not exist, create one.
     * @param email
     * @param alaId
     * @return
     */
    Integer getOrCreateContact(String email, String alaId){
        Integer contactId
        Map contact = getContact(email)
        contactId = contact?.id
        if(!contactId){
            Map props = createContactMap(alaId)
            contact = createContact(props)
            if(contact.id){
                return contact.id
            } else {
                throw new Exception("Could not create a contact for user.")
            }
        }

        contactId
    }

    /**
     * Get contact details of a user.
     * @param email - email address
     * @return
     */
    Map getContact(String email){
        String url = "${grailsApplication.config.collectoryUrl}/contacts/email/${email}"
        Map result = webService.get(url)
        if(result.status in [200, 2001]){
            return result.resp
        } else {
            throw new Exception("Could not get contact with email ${email}")
        }
    }

    /**
     * Create contact details for a user
     * @param props - contains properties like email, first name, fax number etc.
     * @return
     */
    Map createContact(Map props){
        String url = "${grailsApplication.config.collectoryUrl}/contacts/"
        Map result = collectoryHubService.doPost(url, props)
        if(result.status in [200, 201]){
            return  JSON.parse(result.resp)
        } else {
            throw new Exception("Could not create contact")
        }
    }

    /**
     * create a relationship between a contact and an entity
     * @param entity
     * @param uid
     * @param contactId
     * @return
     */
    public createOrUpdateContactForEntity(String entity, String uid, Integer contactId){
        String url = "${grailsApplication.config.collectoryUrl}/${entity}/${uid}/contacts/${contactId}"
        Map entityProps = [:]
        entityProps[DEFAULT_API_KEY_HEADER] = grailsApplication.config.webservice.apiKey
        entityProps[USER_HEADER] = authService.getEmail()
        String body = (entityProps as JSON).toString()
        collectoryHubService.doPost(url, body)
    }

    /**
     * Set status of a dataset to draft
     * @param uid - drt id
     * @return
     */
    Map resetStatus(String uid){
        Map result = grailsApplication.mainContext.collectoryHubRestService.saveTempDataResource([status: 'draft'], uid)
        if(!(result.status in [200, 201])){
            throw new Exception("An error occurred while reseting status")
        }

        result
    }
}
