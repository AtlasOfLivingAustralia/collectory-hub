package au.org.ala.collectory

import au.org.ala.web.CASRoles


class TempDataResourceInterceptor {
    def authService
    def collectoryHubRestService

    TempDataResourceInterceptor() {
        match(controller: "tempDataResource")
    }

    boolean before() {
        //checkUserPrivilege
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

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
