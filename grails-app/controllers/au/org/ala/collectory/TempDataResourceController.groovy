package au.org.ala.collectory

import au.org.ala.web.AlaSecured
import au.org.ala.web.AuthService
import au.org.ala.web.CASRoles
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.util.URIUtil

class TempDataResourceController {
    AuthService authService
    CollectoryHubRestService collectoryHubRestService

    def beforeInterceptor = [action: this.&checkUserPrivilege]

    BootstrapJs getBsVersion() {
        BootstrapJs.valueOf(grailsApplication.config.bs.version ?: "bs2")
    }

    private checkUserPrivilege() {
        if (params.uid) {
            String alaId = authService.getUserId()
            params.isAdmin = authService.userInRole(CASRoles.ROLE_ADMIN)

            Map tempDataResource = collectoryHubRestService.getTempDataResourceFromCollectory(params.uid)
            params.isOwner = alaId == tempDataResource.alaId
            params.canEdit = params.canView = params.isOwner || params.isAdmin
            params.canSubmitForReview = tempDataResource.status == 'draft'
            params.canDecline = params.canLoadToProduction = tempDataResource.status == 'submitted'
            if(tempDataResource.status in ['submitted', 'queuedForLoading']) {
                params.canEdit = false
            }
        }

        return true
    }

    def internalServerError = { text ->
        render(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, text: text)
    }

    /**
     * List user uploaded data sets.
     * @return
     */
    def myData() {
        def currentUserId = authService.getUserId()
        if (currentUserId) {
            def userUploads = collectoryHubRestService.getListOfTempDataResource(currentUserId)
            switch (bsVersion) {
                case BootstrapJs.bs2:
                    render text: "The system does not have a bootstrap 2 version of the requested page"
                    break;
                case BootstrapJs.bs3:
                    render view: 'myData', model: [userUploads: userUploads, currentUserId: currentUserId]
                    break;
            }
        } else {
            login(createLink( action: 'myData', absolute: true))
        }
    }

    /**
     * List all temp data resources uploaded by this system.
     * @return
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectUri = "/")
    def adminList() {
        def currentUserId = authService.getUserId()
        def userUploads = collectoryHubRestService.getListOfTempDataResource('')
        switch (bsVersion) {
            case BootstrapJs.bs2:
                render text: "The system does not have a bootstrap 2 version of the requested page"
                break;
            case BootstrapJs.bs3:
                render view: 'adminList', model: [userUploads: userUploads, currentUserId: currentUserId]
                break;
        }
    }

    /**
     * Edit a temp data resource's metadata.
     * @params uid - drt id
     * @return
     */
    def editMetadata() {
        if (params.canEdit) {
            if (params.uid) {
                Map metadata = collectoryHubRestService.getTempDataResource(params.uid)
                if (metadata) {
                    switch (bsVersion) {
                        case BootstrapJs.bs2:
                            render text: "The system does not have a bootstrap 2 version of the requested page"
                            break;
                        case BootstrapJs.bs3:
                            render view: 'editMetadata', model: metadata
                            break;
                    }
                } else {
                    flash.message = "Could not find data resource."
                    redirect  action: 'myData'
                }
            } else {
                flash.message = "Param missing - uid"
                redirect  action: 'myData'
            }
        } else {
            flash.message = "You cannot edit this data resource."
            redirect(action: 'myData')
        }
    }

    /**
     * This function shows the metadata for a temp data resource.
     * @params uid - drt id
     * @return
     */
    def viewMetadata() {
        if (params.canView) {
            if (params.uid) {
                Map metadata = collectoryHubRestService.getTempDataResource(params.uid)
                if (metadata) {
                    metadata.canEdit = params.canEdit
                    metadata.isAdmin = params.isAdmin
                    metadata.canDecline = params.canDecline
                    metadata.canLoadToProduction = params.canLoadToProduction
                    metadata.canSubmitForReview = params.canSubmitForReview
                    metadata.index = metadata.numberOfRecords <= 200000

                    switch (bsVersion) {
                        case BootstrapJs.bs2:
                            render text: "The system does not have a bootstrap 2 version of the requested page"
                            break;
                        case BootstrapJs.bs3:
                            render view: 'viewMetadata', model: metadata
                            break;
                    }
                } else {
                    flash.message = "Could not find data resource."
                    redirect action: 'myData'
                }
            } else {
                flash.message = "Param missing - uid"
                redirect action: 'myData'
            }
        } else {
            flash.message = "You do not have privilege to view this data resource.  "
            redirect(action: 'myData')
        }
    }

    /**
     * Redirect to CAS login page and provide it an url to redirect after successful login.
     * @param redirectUrl
     */
    private void login(String redirectUrl) {
        String url = "${grailsApplication.config.casServerLoginUrl}?service=${URIUtil.encodeWithinQuery(redirectUrl)}"
        log.debug(url)
        redirect(uri: url)
    }

    /**
     * Save dataset metadata. Metadata edit form posts to this action.
     * @params uid - required - drt id
     * @return
     */
    def saveTempDataResource() {
        Boolean renderEdit = false
        // only people with right privilege can save
        if (params.canEdit) {
            try {
                Map result = collectoryHubRestService.saveTempDateResource(params)
                // check if webservice executed successfully
                if (result?.status == 200 || result?.status == 201) {
                    flash.message = "Successfully saved!"
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                } else {
                    flash.message = 'An error occurred when saving this data.'
                    renderEdit = true
                }
            } catch (Exception e) {
                flash.message = "An error occurred while saving data: ${e.message}"
                renderEdit = true
            }
        } else {
            flash.message = "You do not have privilege to edit this data resource."
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }

        // rendering in this function to preserve user input.
        if (renderEdit) {
            // adds links and other metadata
            collectoryHubRestService.preFillSystemDetails(params)
            switch (bsVersion) {
                case BootstrapJs.bs2:
                    render text: "The system does not have a bootstrap 2 version of the requested page"
                    break;
                case BootstrapJs.bs3:
                    render view: 'editMetadata', model: params
                    break;
            }
        }
    }

    /**
     * Submit a temp data resource for review. This method changes the status to submitted.
     * Only owner of a data resource can call this action.
     * @return
     */
    def submitDataForReview(){
        if(params.canSubmitForReview){
            if(params.isOwner && params.uid){
                Map result = collectoryHubRestService.submitTempDataResourceForReview(params.uid)
                // check if webservice executed successfully
                if(result?.isValid){
                    if (result?.status == 200 || result?.status == 201) {
                        flash.message = "Successfully submitted data resource for review!"
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    } else {
                        flash.message = 'An error occurred when saving this data.'
                        redirect action: 'myData'
                    }
                } else  {
                    flash.message = result.message
                    redirect action: 'editMetadata', params: [uid: params.uid]
                }
            } else {
                flash.message = "Only owners can submit data resource for review."
                redirect action: 'myData'
            }
        } else {
            flash.message = "Only draft data resource can be submitted for review."
            redirect action: 'myData'
        }
    }

    /**
     * This function checks the status of a temp data resource before permitting a delete.
     * When temp data resource is submitted for review, no change is permitted to the data or metadata.
     * This is a proxy action to the actual delete function. This function was created to do some additional checks
     * before redirecting to the actual delete action.
     * @params uid - required - drt id
     * @return
     */
    def delete(){
        if(params.uid) {
            if (params.canEdit) {
//                collectoryHubRestService.deleteTempDataResource(params.uid)
                redirect uri: "${grailsApplication.config.grails.serverURL}/api/myDatasets/deleteResource?uid=${params.uid}"
            } else {
                flash.message = 'Cannot delete since you cannot edit this data resource.'
                redirect action: 'myData'
            }
        } else {
            flash.message = 'Cannot delete since uid was not provided.'
            redirect action: 'myData'
        }
    }

    /**
     * This function checks the status of a temp data resource before permitting a reload.
     * When temp data resource is submitted for review, no change is permitted to the data or metadata.
     * This is a proxy action to the actual reload function. This function was created to do some additional checks
     * before redirecting to the actual reload action.
     * @params uid - required - drt id
     * @return
     */
    def reload() {
        if(params.uid){
            if(params.canEdit){
                collectoryHubRestService.draftTempDataResource(params.uid)
                redirect uri: "${grailsApplication.config.grails.serverURL}/dataCheck?reload=${params.uid}"
            } else {
                flash.message = 'Cannot reload since you cannot edit this data resource.'
                redirect action: 'myData'
            }
        } else {
            flash.message = 'Cannot reload since uid was not provided.'
            redirect action: 'myData'
        }
    }

    /**
     * Decline a submitted temp data resource. This function will update the status of the
     * temp data resource to declined.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectUri = '/')
    def decline(){
        if(params.canDecline){
            if(params.uid){
                Map result = collectoryHubRestService.declineTempDataResource(params.uid)
                if (result?.status == 200 || result?.status == 201) {
                    flash.message = "Data resource has been declined"
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                } else {
                    flash.message = 'An error occurred while saving the data resource.'
                    redirect action: 'myData'
                }
            } else {
                flash.message = "Uid must be provided"
                redirect(action: 'viewMetadata', params: [uid: params.uid])
            }
        } else {
            flash.message = "Cannot decline data. Data resource must be submitted for review first."
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Webservice to load a temp data resource to production.
     * This will create data resource from temp data resource. Then issue Jenkins job to load it into production.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectUri = '/')
    def loadToProduction() {
        try {
            if(params.canLoadToProduction) {
                if(params.uid){
                    Boolean index = params.process in ['index']
                    Map result = collectoryHubRestService.loadToProduction(params.uid, index)
                    if(result.error){
                        flash.message = result.message
                    } else {
                        flash.message = "Successfully loaded to production!"
                    }
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                } else {
                    flash.message = "Uid must be provided"
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = "Cannot load data to production. Data resource must be submitted for review first."
                redirect(action: 'viewMetadata', params: [uid: params.uid])
            }
        } catch (Exception e) {
            flash.message = e.message
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }
}
