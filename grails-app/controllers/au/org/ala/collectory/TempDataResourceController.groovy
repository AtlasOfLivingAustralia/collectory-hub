package au.org.ala.collectory

import au.org.ala.web.AlaSecured
import au.org.ala.web.AuthService
import au.org.ala.web.CASRoles
import org.apache.commons.httpclient.util.URIUtil

class TempDataResourceController {
    AuthService authService
    CollectoryHubRestService collectoryHubRestService
    CollectoryHubJenkinsService collectoryHubJenkinsService

    def beforeInterceptor = [action: this.&checkUserPrivilege]

    BootstrapJs getBsVersion() {
        BootstrapJs.valueOf(grailsApplication.config.bs.version ?: "bs2")
    }

    private checkUserPrivilege() {
        if (params.uid) {
            String alaId = authService.getUserId()
            params.isAdmin = authService.userInRole(CASRoles.ROLE_ADMIN)

            Map tempDataResource = collectoryHubRestService.getTempDataResource(params.uid)
            if(tempDataResource){
                params.isOwner = alaId == tempDataResource.alaId
                params.canEdit = params.canView = params.isOwner || params.isAdmin
                params.canDelete = params.canSubmitForReview = (tempDataResource.status == 'draft') && params.isOwner
                params.canCreateDataResource = params.canDecline = (tempDataResource.status == 'submitted') && params.isAdmin
                params.canLoadToProduction = (tempDataResource.status in ['submitted', 'queuedForLoading']) && params.isAdmin
                params.canReset = (tempDataResource.status != 'draft') && params.isAdmin
                params.canTestRun = params.isAdmin && !!tempDataResource.prodUid
                if((tempDataResource.status in ['submitted', 'queuedForLoading']) && params.canEdit){
                    params.canEdit = false
                }
            }
        }

        return true
    }

    /**
     * error page
     */
    def error(){
        render view: '../systemError'
    }

    def notAuthorised(){
        render view: '../notAuthorised'
    }

    /**
     * List user uploaded data sets.
     * @return
     */
    def myData() {
        try {
            def currentUserId = authService.getUserId()
            if (currentUserId) {
                def userUploads = collectoryHubRestService.getListOfTempDataResource(currentUserId)
                Map model = [userUploads: userUploads, currentUserId: currentUserId]

                switch (bsVersion) {
                    case BootstrapJs.bs2:
                        render text: "The system does not have a bootstrap 2 version of the requested page"
                        break;
                    case BootstrapJs.bs3:
                        render view: 'myData', model: model
                        break;
                }
            } else {
                login(createLink( action: 'myData', absolute: true))
            }
        } catch(Exception e) {
            e.printStackTrace()
            flash.message = "An error occured while accessing your datasets. ${e.message}"
            redirect action: 'error'

        }
    }

    /**
     * List all temp data resources uploaded by this system.
     * @return
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def adminList() {
        try {
            String currentUserId = authService.getUserId()
            List userUploads = collectoryHubRestService.getListOfTempDataResource('')
            Map model = [userUploads: userUploads, currentUserId: currentUserId]
            switch (bsVersion) {
                case BootstrapJs.bs2:
                    render text: "The system does not have a bootstrap 2 version of the requested page"
                    break;
                case BootstrapJs.bs3:
                    render view: 'adminList', model: model
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace()
            flash.message = "An error occurred while accessing dataset list ${e.message}"
            redirect action: 'error'
        }
    }

    /**
     * Edit a temp data resource's metadata.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "error")
    def editMetadata() {
        try {
            if (params.uid) {
                if (params.canEdit) {
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
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    }
                } else {
                    flash.message = "You cannot edit this data resource."
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = "Param missing - uid"
                redirect  action: 'myData'
            }
        } catch (Exception e) {
            e.printStackTrace()
            flash.message = "An error occurred while accessing dataset list ${e.message}"
            redirect action: 'error'
        }
    }

    /**
     * This function shows the metadata for a temp data resource.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "error")
    def viewMetadata() {
        try {
            if (params.uid) {
                if (params.canView) {
                    Map metadata = collectoryHubRestService.getTempDataResource(params.uid)
                    if (metadata) {
                        metadata.canEdit = params.canEdit
                        metadata.isAdmin = params.isAdmin
                        metadata.canDecline = params.canDecline
                        metadata.canLoadToProduction = params.canLoadToProduction
                        metadata.canSubmitForReview = params.canSubmitForReview
                        metadata.canReset = params.canReset
                        metadata.canCreateDataResource = params.canCreateDataResource
                        metadata.canTestRun = params.canTestRun
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
                    flash.message = "You do not have privilege to view this data resource.  "
                    redirect(action: 'myData')
                }
            } else {
                flash.message = "Param missing - uid"
                redirect action: 'myData'
            }
        } catch (Exception e) {
            e.printStackTrace()
            flash.message = "An error occurred while accessing dataset list ${e.message}"
            redirect action: 'error'
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
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "error")
    def saveTempDataResource() {
        try {
            // only people with right privilege can save
            if (params.canEdit) {
                Map result = collectoryHubRestService.saveTempDataResource(params, params.uid)
                // check if webservice executed successfully
                if (result?.status == 200 || result?.status == 201) {
                    flash.message = "Successfully saved!"
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                } else {
                    flash.message = 'An error occurred when saving this data.'
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
            } else {
                flash.message = "You do not have privilege to edit this data resource."
                redirect(action: 'viewMetadata', params: [uid: params.uid])
            }
        } catch (Exception e) {
            e.printStackTrace()
            flash.message = "An error occurred while saving a dataset. ${e.message}"
            redirect action: 'error'
        }
    }

    /**
     * Submit a temp data resource for review. This method changes the status to submitted.
     * Only owner of a data resource can call this action.
     * @return
     */
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "error")
    def submitDataForReview(){
        try {
            if(params.uid){
                if(params.canSubmitForReview){
                        Map result = collectoryHubRestService.submitTempDataResourceForReview(params.uid)
                        // check if webservice executed successfully
                        if(result?.isValid){
                            if (result?.status in [200, 201]) {
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
                    flash.message = "Only draft data resource can be submitted for review."
                    redirect action: 'myData'
                }
            } else {
                flash.message = "Param missing - uid"
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error(e.message)
            e.printStackTrace()
            flash.message = "And error occured while submitting data from review. ${e.message}"
            redirect(action: 'viewMetadata', params: [uid: params.uid])
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
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "error")
    def delete(){
        try {
            if(params.uid) {
                if (params.canEdit) {
                    collectoryHubRestService.deleteTempDataResource(params.uid)
                    flash.message = "Deleted dataset ${params.uid}"
                    redirect action: 'myData'
                } else {
                    flash.message = 'Cannot delete since you cannot edit this data resource.'
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = 'Cannot delete since uid was not provided.'
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error(e.message)
            e.printStackTrace()
            flash.message = "And error occured while deleting. ${e.message}"
            redirect(action: 'viewMetadata', params: [uid: params.uid])
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
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "error")
    def reload() {
        try {
            if(params.uid){
                if(params.canEdit){
                    collectoryHubRestService.draftTempDataResource(params.uid)
                    redirect uri: "${grailsApplication.config.grails.serverURL}/dataCheck/reload?dataResourceUid=${params.uid}"
                } else {
                    flash.message = 'Cannot reload since you cannot edit this data resource.'
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = 'Cannot reload since uid was not provided.'
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error(e.message)
            e.printStackTrace()
            flash.message = "And error occured while reloading. ${e.message}"
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Decline a submitted temp data resource. This function will update the status of the
     * temp data resource to declined.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = "tempDataResource", redirectAction = "error")
    def decline(){
        try {
            if(params.uid){
                if(params.canDecline){
                    Map result = collectoryHubRestService.declineTempDataResource(params.uid)
                    if (result?.status == 200 || result?.status == 201) {
                        flash.message = "Data resource has been declined"
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    } else {
                        flash.message = 'An error occurred while saving the data resource.'
                        redirect action: 'myData'
                    }
                } else {
                    flash.message = "Cannot decline data. Data resource must be submitted for review first."
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = "Uid must be provided"
                redirect(action: 'adminList')
            }
        } catch (Exception e) {
            log.error(e.message)
            e.printStackTrace()
            flash.message = "And error occured while declining a dataset. ${e.message}"
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Webservice to load a temp data resource to production.
     * This will create data resource from temp data resource. Then issue Jenkins job to load it into production.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = "tempDataResource", redirectAction = "error")
    def loadToProduction() {
        try {
            if(params.uid) {
                if(params.canLoadToProduction) {
                    Boolean index = params.process in ['index']
                    Map result = collectoryHubRestService.loadToProduction(params.uid, index)
                    if(result.error){
                        flash.message = result.message
                    } else {
                        flash.message = "Successfully loaded to production!"
                    }
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                } else {
                    flash.message = "Cannot load data to production. Data resource must be submitted for review first."
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = "Uid must be provided"
                redirect(action: 'viewMetadata', params: [uid: params.uid])
            }
        } catch (Exception e) {
            flash.message = "An error occurred while loading to production. ${e.message}"
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Test run a production ready dataset.
     * @params uid - drt id
     * @params
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = 'tempDataResource', redirectAction = 'myData')
    def testRun(){
        try {
            if(params.uid){
                if(params.canLoadToProduction){
                    Map result = collectoryHubJenkinsService.testRun(params.uid)
                    redirect(uri: "/jenkins/console/${result.jobName}/${result.buildNumber}/${result.start}?uid=${params.uid}")
                } else {
                    flash.message="You can only test run after the dataset is production ready."
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message="Parameter uid is necessary"
                redirect(action: 'myData')
            }
        } catch (Exception e) {
            flash.message = "An error occurred while doing a test run. ${e.message}"
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Test run a production ready dataset.
     * @params uid - drt id
     * @params
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = 'tempDataResource', redirectAction = 'myData')
    def resetStatus(){
        try {
            if(params.uid){
                try{
                    collectoryHubRestService.resetStatus(params.uid)
                    flash.message = "Successfully reset status."
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                } catch (Exception e){
                    flash.message = "An error occurred while trying to set status. ${e.message}"
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message="Parameter uid is necessary"
                redirect(action: 'myData')
            }
        } catch (Exception e) {
            flash.message = "An error occurred while reseting dataset status. ${e.message}"
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Test run a production ready dataset.
     * @params uid - drt id
     * @params
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = 'tempDataResource', redirectAction = 'myData')
    def createDr(){
        try {
            if(params.canCreateDataResource){
                if(params.uid){
                    try{
                        def (String drId, Map drt)= collectoryHubRestService.createOrSaveDataResource(params.uid)
                        if(drId){
                            flash.message = "Successfully created data resource!"
                        } else {
                            flash.message = "Failed to create data resource."
                        }

                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    } catch (Exception e){
                        flash.message = "An error occurred while trying to set status. ${e.message}"
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    }
                } else {
                    flash.message="Parameter uid is necessary"
                    redirect(action: 'myData')
                }
            } else {
                flash.message = "You cannot create a data resource."
                redirect(action: 'viewMetadata', params: [uid: params.uid])
            }
        } catch (Exception e){
            flash.message = "An error occurred while creating a new data resource"
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }
}
