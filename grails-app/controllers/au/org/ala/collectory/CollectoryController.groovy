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
    def grailsApplication

    BootstrapJs getBsVersion() {
        BootstrapJs.valueOf(grailsApplication.config.bs.version ?: "bs2")
    }

    /**
     * Render page to list data resources
     * @return
     */
    def list() {
        switch (bsVersion) {
            case BootstrapJs.bs2:
                render view: 'list'
                break
            case BootstrapJs.bs3:
                render text: "The system does not have a bootstrap 3 version of the requested page"
                break
        }
    }

    /**
     * A webservice to get all data resources for a hub or all ALA data
     * @params source - hub or all
     * @return
     */
    def resources() {
        List drs;
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
        List drs = collectoryHubRestService.getFilteredDataResources(query, source);
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
            switch (bsVersion) {
                case BootstrapJs.bs2:
                    render view: 'showDataResource', model: [instance: instance]
                    break;
                case BootstrapJs.bs3:
                    render text: "The system does not have a bootstrap 3 version of the requested page"
                    break;
            }
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
            switch (bsVersion) {
                case BootstrapJs.bs2:
                    render view: 'showInstitution', model: [instance: instance]
                    break;
                case BootstrapJs.bs3:
                    render text: "The system does not have a bootstrap 3 version of the requested page"
                    break;
            }
        } else {
            render status: HttpStatus.SC_NOT_FOUND, text: 'Data provider not found'
        }

    }
}
