package au.org.ala.collectory

import grails.converters.JSON
import org.apache.http.HttpStatus

class DatasetsController {
    CollectoryHubRestService collectoryHubRestService
    def grailsApplication

    BootstrapJs getBsVersion(){
        BootstrapJs.valueOf(grailsApplication.config.bs.version?:"bs2")
    }

    /**
     * Render page to list data resources
     * @return
     */
    def list() {
        switch (bsVersion){
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
    def resources(){
        List drs;
        String source = params.source?:'hub'
        drs = collectoryHubRestService.getDataResources(source)
        render text: drs as JSON, contentType: 'application/json'
    }

    /**
     * a server side filtering of data resources based on client side querying
     * @params - q - query string
     * @params - source - hub or all
     * @return ['dr1','dr2']
     */
    def dataSetSearch (){
        String source = params.source?:'hub'
        String query = params.q?:''
        List drs = collectoryHubRestService.getFilteredDataResources(query, source);
        render drs as JSON
    }

    /**
     * Get details for a data resource id and display it on a view.
     * @params id - data resource id
     * @return
     */
    def showDataResource(){
        String id = params.id
        if(id){
            Map instance = collectoryHubRestService.getDataResource(id)
            switch (bsVersion){
                case BootstrapJs.bs2:
                    render view: 'showDataResource', model:[instance: instance]
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
    def showInstitution(){
        String id = params.id
        if(id){
            Map instance = collectoryHubRestService.getInstitution(id)
            switch (bsVersion){
                case BootstrapJs.bs2:
                    render view: 'showInstitution', model:[instance: instance]
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
