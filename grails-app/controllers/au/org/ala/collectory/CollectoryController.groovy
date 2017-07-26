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

import grails.converters.JSON
import org.apache.http.HttpStatus

class CollectoryController {
    CollectoryHubRestService collectoryHubRestService

    /**
     * Render page to list data resources
     * @return
     */
    def list() {
        render view: 'list'
    }

    /**
     * A webservice to get all data resources for a hub or all ALA data
     * @params source - hub or all
     * @return
     */
    def resources() {
        List drs
        String source = params.source ?: 'hub'
        drs = collectoryHubRestService.getDataResources(source)
        render text: drs as JSON, contentType: 'application/json'
    }

    /**
     * a server side filtering of data resources based on client side querying
     * @params - q - query string
     * @params - source - hub or all
     * @return ['dr1','dr2']
     */
    def dataSetSearch() {
        String source = params.source ?: 'hub'
        String query = params.q ?: ''
        List drs = collectoryHubRestService.getFilteredDataResources(query, source)
        render drs as JSON
    }

    /**
     * Get details for a data resource id and display it on a view.
     * @params id - data resource id
     * @return
     */
    def showDataResource() {
        String id = params.id
        if (id) {
            Map instance = collectoryHubRestService.getDataResource(id)
            render view: 'showDataResource', model: [instance: instance]
        } else {
            render status: HttpStatus.SC_NOT_FOUND, text: 'Data provider not found'
        }
    }

    /**
     * get details for an institution
     * @params id - institution id
     * @return
     */
    def showInstitution() {
        String id = params.id
        if (id) {
            Map instance = collectoryHubRestService.getInstitution(id)
            render view: 'showInstitution', model: [instance: instance]
        } else {
            render status: HttpStatus.SC_NOT_FOUND, text: 'Data provider not found'
        }

    }
}
