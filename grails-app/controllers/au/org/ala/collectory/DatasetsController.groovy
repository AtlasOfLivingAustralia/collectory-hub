package au.org.ala.collectory

import au.org.ala.web.AuthService
import grails.converters.JSON
import org.apache.http.HttpStatus

import javax.annotation.PostConstruct

class DatasetsController {
    CollectoryRestService collectoryRestService
    def grailsApplication
    AuthService authService

    BootstrapJs bsVersion

    @PostConstruct
    init(){
        bsVersion = BootstrapJs.valueOf(grailsApplication.config.bs.version?:"bs2")
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
        drs = collectoryRestService.getDataResources(source)
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
        List drs = collectoryRestService.getFilteredDataResources(query, source);
        render drs as JSON
    }

    /**
     * Get details for a data resource id and display it on a view.
     * @params id - data resource id
     * @return
     */
    def showDataResource(){
        Boolean flag = authService.userInRole("ROLE_BASE")
        String id = params.id
        if(id){
            Map instance = collectoryRestService.getDataResource(id)
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
            Map instance = collectoryRestService.getInstitution(id)
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
