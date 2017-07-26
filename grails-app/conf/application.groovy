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
 * Created by Temi on 11/07/2016.
 */

hub.queryContext = "data_hub_uid:dh10"
biocache.queryContext = "cl2110:\"Murray-Darling Basin Boundary\""

ala.baseURL = "http://www.ala.org.au"
bie.baseURL = "http://bie.ala.org.au"
biocacheService.baseURL = "http://biocache.ala.org.au/ws"
biocache.baseURL = "http://biocache.ala.org.au"
collectory.baseURL = "http://collections.ala.org.au"
logger.baseURL = "http://logger.ala.org.au/service"
speciesList.baseURL = "http://lists.ala.org.au/speciesListItem/list/"

alertResourceName = "ALA"
collectory.resources = "http://collections.ala.org.au/public/resources.json"

skin.appName = "Collectory hub"
skin.layout = "main"
skin.orgNameLong = "Atlas of Living Australia"
skin.orgNameShort = "ALA"
skin.fluidLayout = false

// Disable UI components
disableOverviewMap = "true"
disableAlertLinks = "true"
disableLoggerLinks = "false"

adminRole = "ROLE_ADMIN"

sandbox.hideCharts = "false"

grails.cache.config = {
    defaults {
        eternal false
        overflowToDisk false
        maxElementsInMemory 10000
        timeToLiveSeconds 3600
    }

    cache {
        name 'collectoryCache'
        timeToLiveSeconds(3600 * 4)
    }

    cache {
        name 'longTermCache'
        timeToLiveSeconds(3600 * 24)
    }
}