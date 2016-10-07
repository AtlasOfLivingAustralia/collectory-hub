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

import au.org.ala.web.AlaSecured
import au.org.ala.web.AuthService
import au.org.ala.web.CASRoles
import org.apache.commons.httpclient.util.URIUtil

/**
 * All interactions with Temp Data Resource collectory instance is done through this controller.
 * Actions like listing all Temp Data Resources and  a list of user uploaded Temp Data Resources is added here.
 * This controller can view, edit and delete a Temp Data Resource. It can create a Data resource from a Temp Data Resource.
 */
class TempDataResourceController {
    AuthService authService
    CollectoryHubRestService collectoryHubRestService
    CollectoryHubJenkinsService collectoryHubJenkinsService

    BiocacheRestService biocacheRestService
    FormatService formatService

    def beforeInterceptor = [action: this.&checkUserPrivilege]

    BootstrapJs getBsVersion() {
        BootstrapJs.valueOf(grailsApplication.config.bs.version ?: "bs2")
    }

    private Boolean checkUserPrivilege() {
        if (params.uid) {
            String alaId = authService.getUserId()
            params.isAdmin = authService.userInRole(CASRoles.ROLE_ADMIN)

            Map tempDataResource = collectoryHubRestService.getTempDataResource(params.uid)
            if (tempDataResource) {
                params.isOwner = alaId == tempDataResource.alaId
                params.canEdit = params.canView = params.isOwner || params.isAdmin
                params.canDelete = params.canSubmitForReview = (tempDataResource.status == 'draft') && params.isOwner
                params.canCreateDataResource = params.canDecline = (tempDataResource.status == 'submitted') && params.isAdmin
                params.canLoadToProduction = (tempDataResource.status in ['submitted', 'queuedForLoading']) && params.isAdmin
                params.canReset = (tempDataResource.status != 'draft') && params.isAdmin
                params.canTestRun = params.isAdmin && !!tempDataResource.prodUid
                if ((tempDataResource.status in ['submitted', 'queuedForLoading']) && params.canEdit) {
                    params.canEdit = false
                }
            }
        }

        return true
    }

    /**
     * error page
     */
    def error() {
        render view: '../systemError'
    }

    def notAuthorised() {
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
                String max = params.max?:10, offset = params.offset?:0, status = params.status?:'',
                       sortField = params.sortField?:'lastUpdated', sortOrder = params.sortOrder?:'desc'
                Map userUploads = collectoryHubRestService.getListOfTempDataResource(currentUserId, max, offset, status, sortField, sortOrder)
                userUploads.currentUserId = currentUserId
                userUploads.params = [max      : max, offset: offset, status: status, sortField: sortField, sortOrder: sortOrder]
                userUploads.statuses = collectoryHubRestService.TEMP_DATA_RESROUCE_STATUSES
                switch (bsVersion) {
                    case BootstrapJs.bs2:
                        render text: message(code: "bs2.notFoundMessage", default: "The system does not have a bootstrap 2 version of the requested page")
                        break;
                    case BootstrapJs.bs3:
                        render view: 'myData', model: userUploads
                        break;
                }
            } else {
                login(createLink(action: 'myData', absolute: true))
            }
        } catch (Exception e) {
            log.error(e.message, e)
            flash.message = message(code: "tempDataResource.myData.exception", default: "An error occured while accessing your datasets.")
            redirect action: 'error'
        }
    }


    def adminChartOptions () {
        params.origin = 'adminList'
        forward(action: 'chartOptions', params: params)
    }
    /**
     * Retrieves data set chart configuration
     */

    def chartOptions(){
        //retrieve the current chart options
        //retrieve the list of custom indexes...

        log.debug("Origin: ${params.origin}")
        def metadata = collectoryHubRestService.getTempDataResourceFromCollectory(params.tempUid)
        def customIndexes = biocacheRestService.getCustomIndexes(params.tempUid)
        def chartConfig = biocacheRestService.getChartOptions(params.tempUid)

        if(!chartConfig){
            chartConfig = []
            customIndexes.each {
                chartConfig << [
                        field: it,
                        format: 'pie',
                        visible: true
                ]
            }
        }

        chartConfig.each { cfg ->
            cfg.formattedField = formatService.formatFieldName(cfg.field)
        }

        def instance = [metadata: metadata, chartConfig: chartConfig, tempUid: params.tempUid]
        respond(instance, view:"chartOptions", model: instance)
    }

    /**
     * Save new data set chart configuration
     * @return
     */
    def saveChartOptions(){

        def chartOptions = []
        if (request.contentType?.startsWith('application/json')) {
            chartOptions = request.getJSON()
        } else { // form encoded
            def fields = params.field
            def format = params.format

            fields.eachWithIndex { field, idx ->

                def visibleFlag = 'visible_' + idx
                def values = params[visibleFlag]
                def visible = {
                    if(values instanceof String[]){
                        true
                    } else {
                        false
                    }
                }.call()
                chartOptions << [
                        field: field,
                        format: format[idx],
                        visible: visible
                ]
            }
        }
        def uid = params.tempUid
        log.debug("Saving chart options for $uid: $chartOptions")
        def status = [status: biocacheRestService.saveChartOptions(uid, chartOptions)]

        String action =  params.origin != 'adminList'? 'myData' : 'adminList'

        request.withFormat {
            form multipartForm {
                redirect( controller: "tempDataResource", action: action, method: 'GET')
            }
            '*'{ respond status }
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
            String max = params.max?:10, offset = params.offset?:0, status = params.status?:'',
                    sortField = params.sortField?:'lastUpdated', sortOrder = params.sortOrder?:'desc'
            Map userUploads = collectoryHubRestService.getListOfTempDataResource('', max, offset, status, sortField, sortOrder)
            userUploads.currentUserId = currentUserId
            userUploads.params = [max      : max, offset: offset, status: status, sortField: sortField, sortOrder: sortOrder]
            userUploads.statuses = collectoryHubRestService.TEMP_DATA_RESROUCE_STATUSES
            switch (bsVersion) {
                case BootstrapJs.bs2:
                    render text: message(code: "bs2.notFoundMessage", default: "The system does not have a bootstrap 2 version of the requested page")
                    break;
                case BootstrapJs.bs3:
                    render view: 'adminList', model: userUploads
                    break;
            }
        } catch (Exception e) {
            log.error (e.message, e)
            flash.message = message(code: "tempDataResource.adminList.exception", default: "An error occurred while accessing all datasets list.")
            redirect action: 'error'
        }
    }

    /**
     * Edit a temp data resource's metadata.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def editMetadata() {
        try {
            if (params.uid) {
                if (params.canEdit) {
                    Map metadata = collectoryHubRestService.getTempDataResource(params.uid)
                    if (metadata) {
                        switch (bsVersion) {
                            case BootstrapJs.bs2:
                                render text: message(code: "bs2.notFoundMessage", default: "The system does not have a bootstrap 2 version of the requested page")
                                break;
                            case BootstrapJs.bs3:
                                render view: 'editMetadata', model: metadata
                                break;
                        }
                    } else {
                        flash.message = message(code: "tempDataResource.drtNotFound", args: [params.uid], default: "Could not find dataset {0}.")
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    }
                } else {
                    flash.message = message(code: "tempDataResource.editMetadata.cantEdit", args: [params.uid], default: "You cannot edit dataset {0}.")
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error (e.message, e)
            flash.message = message(code: "tempDataResource.editMetadata.exception", args: [params.uid], default: "An error occurred while editing dataset {0}.")
            redirect action: 'error'
        }
    }

    /**
     * This function shows the metadata for a temp data resource.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def viewMetadata() {
        try {
            if (params.uid) {
                if (params.canView) {
                    Map metadata = collectoryHubRestService.getTempDataResource(params.uid)
                    if (metadata) {
                        metadata.canEdit = params.canEdit
                        metadata.isAdmin = params.isAdmin
                        metadata.isOwner = params.isOwner
                        metadata.canDecline = params.canDecline
                        metadata.canLoadToProduction = params.canLoadToProduction
                        metadata.canSubmitForReview = params.canSubmitForReview
                        metadata.canReset = params.canReset
                        metadata.canCreateDataResource = params.canCreateDataResource
                        metadata.canTestRun = params.canTestRun
                        metadata.index = metadata.numberOfRecords <= 200000

                        switch (bsVersion) {
                            case BootstrapJs.bs2:
                                render text: message(code: "bs2.notFoundMessage", default: "The system does not have a bootstrap 2 version of the requested page")
                                break;
                            case BootstrapJs.bs3:
                                render view: 'viewMetadata', model: metadata
                                break;
                        }
                    } else {
                        flash.message = message(code: "tempDataResource.drtNotFound", args: [params.uid], default: "Could not find dataset {0}.")
                        redirect action: 'myData'
                    }
                } else {
                    flash.message = message(code: "tempDataResource.viewMetadata.cantView", args: [params.uid], default: "You do not have privilege to view dataset {0}.")
                    redirect(action: 'myData')
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error (e.message, e)
            flash.message = message(code: "tempDataResource.viewMetadata.exception", default: "An error occurred while viewing dataset {0}.")
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
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def saveTempDataResource() {
        try {
            if (params.uid) {
                // only people with right privilege can save
                if (params.canEdit) {
                    Map result = collectoryHubRestService.saveTempDataResource(params, params.uid)
                    // check if webservice executed successfully
                    if (result?.status == 200 || result?.status == 201) {
                        flash.message = message(code: "tempDataResource.saveTempDataResource.success", default: "Successfully saved!")
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    } else {
                        flash.message = message(code: "tempDataResource.saveTempDataResource.failed", args: [params.uid], default: "An error occurred when saving dataset {0}.")
                        // adds links and other metadata
                        collectoryHubRestService.preFillSystemDetails(params)
                        switch (bsVersion) {
                            case BootstrapJs.bs2:
                                render text: message(code: "bs2.notFoundMessage", default: "The system does not have a bootstrap 2 version of the requested page")
                                break;
                            case BootstrapJs.bs3:
                                render view: 'editMetadata', model: params
                                break;
                        }
                    }
                } else {
                    flash.message = message(code: "tempDataResource.saveTempDataResource.noPrivilege", args: [params.uid], default: "You do not have privilege to edit dataset {0}")
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error (e.message, e)
            flash.message = message(code: "tempDataResource.saveTempDataResource.noPrivilege", args: [params.uid], default: "An error occurred while saving dataset {0}.")
            redirect action: 'error'
        }
    }

    /**
     * Submit a temp data resource for review. This method changes the status to submitted.
     * Only owner of a data resource can call this action.
     * @return
     */
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def submitDataForReview() {
        try {
            if (params.uid) {
                if (params.canSubmitForReview) {
                    Map result = collectoryHubRestService.submitTempDataResourceForReview(params.uid)
                    // check if webservice executed successfully
                    if (result?.isValid) {
                        if (result?.status in [200, 201]) {
                            flash.message = message(code: "tempDataResource.submitDataForReview.success", args: [params.uid], default: "Successfully submitted dataset {0} for review!")
                            redirect(action: 'viewMetadata', params: [uid: params.uid])
                        } else {
                            flash.message = message(code: "tempDataResource.submitDataForReview.failed", args: [params.uid], default: "An error occurred while submitting dataset for review {0}")
                            redirect action: 'myData'
                        }
                    } else {
                        flash.message = message(code: "tempDataResource.submitDataForReview.invalid", args: result.args, default: "The following fields are mandatory - {0}")
                        redirect action: 'editMetadata', params: [uid: params.uid]
                    }
                } else {
                    flash.message = message(code: "tempDataResource.submitDataForReview.cantSubmit", default: "Only draft data resource can be submitted for review.")
                    redirect action: 'myData'
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error (e.message, e)
            flash.message = message(code: "tempDataResource.submitDataForReview.exception", args: [params.uid], default: "And error occured while submitting data from review.")
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
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def delete() {
        try {
            if (params.uid) {
                if (params.canEdit) {
                    collectoryHubRestService.deleteTempDataResource(params.uid)
                    flash.message = message(code: "tempDataResource.delete.success", args: [params.uid], default: "Deleted dataset {0}")
                    redirect action: 'myData'
                } else {
                    flash.message = message(code: "tempDataResource.delete.failed", args: [params.uid], default: 'You do not have privilege to delete dataset {0}.')
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error (e.message, e)
            flash.message = message(code: "tempDataResource.delete.exception", args: [params.uid], default: "An error occurred while deleting dataset {0}.")
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
    @AlaSecured(value = "ROLE_USER", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def reload() {
        try {
            if (params.uid) {
                if (params.canEdit) {
                    collectoryHubRestService.draftTempDataResource(params.uid)
                    redirect uri: "${grailsApplication.config.grails.serverURL}/dataCheck/reload?dataResourceUid=${params.uid}"
                } else {
                    flash.message = message(code: "tempDataResource.reload.failed", args: [params.uid], default: 'You do not have privilege to rewrite dataset {0}.')
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect action: 'myData'
            }
        } catch (Exception e) {
            log.error (e.message, e)
            flash.message = message(code: "tempDataResource.reload.exception", args: [params.uid], default: "An error occurred while rewriting dataset {0}.")
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Decline a submitted temp data resource. This function will update the status of the
     * temp data resource to declined.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def decline() {
        try {
            if (params.uid) {
                if (params.canDecline) {
                    Map result = collectoryHubRestService.declineTempDataResource(params.uid)
                    if (result?.status == 200 || result?.status == 201) {
                        flash.message = message(code: "tempDataResource.decline.success", args: [params.uid], default: "Declined dataset {0}")
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    } else {
                        flash.message = message(code: "tempDataResource.decline.error", args: [params.uid], default: 'An error occurred while updating status to dataset {0}.')
                        redirect action: 'myData'
                    }
                } else {
                    flash.message = message(code: "tempDataResource.decline.failed", args: [params.uid], default: "Cannot decline dataset {0}. Data resource must be submitted for review first.")
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect(action: 'adminList')
            }
        } catch (Exception e) {
            log.error (e.message, e)
            flash.message = message(code: "tempDataResource.decline.exception", args: [params.uid], default: "And error occurred while declining dataset {0}.")
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Webservice to load a temp data resource to production.
     * This will create data resource from temp data resource. Then issue Jenkins job to load it into production.
     * @params uid - drt id
     * @return
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = "tempDataResource", redirectAction = "notAuthorised")
    def loadToProduction() {
        try {
            if (params.uid) {
                if (params.canLoadToProduction) {
                    Boolean index = params.process in ['index']
                    Map result = collectoryHubRestService.loadToProduction(params.uid, index)
                    if (result.buildNumber && result.jobName) {
                        redirect(uri: "/jenkins/console/${result.jobName}/${result.buildNumber}/${result.start}?uid=${params.uid}")
                    } else {
                        flash.message = message(code: "tempDataResource.loadToProduction.failed", default: "Failed to create job on jenkins.")
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    }
                } else {
                    flash.message = message(code: "tempDataResource.loadToProduction.cantLoad", default: "Cannot load data to production. Data resource must be submitted for review first.")
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect(action: 'viewMetadata', params: [uid: params.uid])
            }
        } catch (Exception e) {
            log.error(e.message, e)
            flash.message = message(code: "tempDataResource.loadToProduction.exception", default: "An error occurred while loading dataset to production.")
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Test run a production ready dataset.
     * @params uid - drt id
     * @params
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = 'tempDataResource', redirectAction = 'myData')
    def testRun() {
        try {
            if (params.uid) {
                if (params.canTestRun) {
                    Map result = collectoryHubJenkinsService.testRun(params.uid)
                    if (result.buildNumber && result.jobName) {
                        redirect(uri: "/jenkins/console/${result.jobName}/${result.buildNumber}/${result.start}?uid=${params.uid}")
                    } else {
                        flash.message = message(code: "tempDataResource.testRun.failed", default: "Failed to submit job on jenkins.")
                        redirect(action: 'viewMetadata', params: [uid: params.uid])
                    }
                } else {
                    flash.message = message(code: "tempDataResource.testRun.cantTestRun", args: [params.uid], default: "You can only test run after a data resource is created for dataset {0}.")
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect(action: 'myData')
            }
        } catch (Exception e) {
            log.error(e.message, e)
            flash.message = message(code: "tempDataResource.testRun.exception", default: "An error occurred while doing a test run.")
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Test run a production ready dataset.
     * @params uid - drt id
     * @params
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = 'tempDataResource', redirectAction = 'myData')
    def resetStatus() {
        try {
            if (params.uid) {
                collectoryHubRestService.resetStatus(params.uid)
                flash.message = message(code: "tempDataResource.resetStatus.success", default: "Successfully reset status.")
                redirect(action: 'viewMetadata', params: [uid: params.uid])
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect(action: 'myData')
            }
        } catch (Exception e) {
            log.error(e.message, e)
            flash.message = message(code: "tempDataResource.resetStatus.exception", default: "An error occurred while resetting dataset status.")
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }

    /**
     * Test run a production ready dataset.
     * @params uid - drt id
     * @params
     */
    @AlaSecured(value = "ROLE_ADMIN", redirectController = 'tempDataResource', redirectAction = 'myData')
    def createDr() {
        try {
            if (params.uid) {
                if (params.canCreateDataResource) {
                    def (String drId, Map drt) = collectoryHubRestService.createOrSaveDataResource(params.uid)
                    if (drId) {
                        flash.message = message(code: "tempDataResource.createDr.success", default: "Successfully created data resource!")
                    } else {
                        flash.message = message(code: "tempDataResource.createDr.failed", default: "Failed to create data resource.")
                    }

                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                } else {
                    flash.message = message(code: "tempDataResource.createDr.noPrivilege", default: "You do not have privilege to create a data resource")
                    redirect(action: 'viewMetadata', params: [uid: params.uid])
                }
            } else {
                flash.message = message(code: "tempDataResource.missingUid", default: "Param missing - uid")
                redirect(action: 'myData')
            }
        } catch (Exception e) {
            log.error(e.message, e)
            flash.message = message(code: "tempDataResource.createDr.exception", default: "An error occurred while creating a new data resource")
            redirect(action: 'viewMetadata', params: [uid: params.uid])
        }
    }
}
