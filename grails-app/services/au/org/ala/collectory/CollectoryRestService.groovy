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

import au.org.ala.ws.service.WebService
import grails.plugin.cache.Cacheable
import org.apache.commons.httpclient.util.URIUtil

import javax.annotation.PostConstruct

class CollectoryRestService {
    def grailsApplication
    WebService webService

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
}
