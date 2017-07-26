<!--
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
 * Created on 13/04/16.
 */
-->
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>${grailsApplication.config.skin.orgNameLong}</title>

    <g:set var="defaultSource" value="hub"></g:set>

    <meta name="deferred-js" content=""/>

    <asset:stylesheet src="collectory-hub"/>
    <asset:javascript src="head"/>
    <asset:javascript src="jquery-extensions"/>

    <script type="text/javascript">
        var altMap = true, hubSource = "${defaultSource}";
        $(document).ready(function() {
            loadResources("${grailsApplication.config.grails.serverURL}", '${defaultSource}');
            $('select#per-page').change(onPageSizeChange);
            $('select#sort').change(onSortChange);
            $('select#dir').change(onDirChange);
        });
        var COLLECTORY_CONF = { contextPath: "${grailsApplication.config.contextPath}", locale: "" }

        $(document).ready(function() {

            // Hub vs All records toggle
            $("[name='hub-toggle']").bootstrapSwitch({
                size: "small",
                onText: "All",
                onColor: "primary",
                offText: "<g:message code="button.toggle.hubText" default="Hub" />",
                offColor: "success",
                onSwitchChange: function(event, state) {
                    if (!state) {
                        // hub visible
                        hubSource = 'hub'
                        loadResources("${grailsApplication.config.grails.serverURL}", 'hub')
                    } else {
                        hubSource = 'all'
                        loadResources("${grailsApplication.config.grails.serverURL}", 'all')
                    }
                }
            });
        });
    </script>

    <asset:javascript src="collectory-hub"/>

</head>

<body id="page-datasets" class="nav-datasets">
<div id="content">
    <div id="header">
        <div class="full-width">
            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <div>
                <h1><g:message code="public.datasets.title" /></h1>
                <p style="padding-bottom:6px !important;"><g:message code="public.datasets.header.message01" /> ${grailsApplication.config.projectName}, <g:message code="public.datasets.header.message02" />.</p>
                <p><g:message code="public.datasets.header.message03" /> <img style="vertical-align:middle;" src="${resource(dir:'/images',file:'skin/ExpandArrow.png')}"/><g:message code="public.datasets.header.message04" />.</p>
            </div><!--close hrgroup-->
        </div><!--close section-->
    </div><!--close header-->

    <noscript>
        <div class="noscriptmsg">
            <g:message code="public.datasets.noscript.message01" />.
        </div>
    </noscript>

    <div class="collectory-content row">
        <div id="sidebarBoxXXX" class="col-sm-3 facets well well-small">
            <div class="sidebar-header">
                <h3><g:message code="public.datasets.sidebar.header" /></h3>
            </div>

            <div id="currentFilterHolder">
            </div>

            <div id="dsFacets">
            </div>
        </div>

        <div id="data-set-list" class="col-sm-9">
            <div class="well">
                <div class="row">
                    <div class="pull-left">
                        <span id="resultsReturned"><g:message code="public.datasets.resultsreturned.message01" /> <strong></strong>&nbsp;<g:message code="public.datasets.resultsreturned.message02" />.</span>
                        <div class="input-append">
                            <input type="text" name="dr-search" id="dr-search"/>
                            <a href="javascript:void(0);" title="Only show data sets which contain the search term" id="dr-search-link" class="btn"><g:message code="public.datasets.drsearch.search" /></a>
                            <a href="javascript:void(0);" id="reset"><a href="javascript:reset()" title="Remove all filters and sorting options" class="btn"><g:message code="public.datasets.drsearch.resetlist" /></a></a>
                        </div>
                    </div>
                    <div class="pull-right">
                        <div class="activeFilters">
                            <g:message code="button.toggle.label" default="All / Hub datasets" /> <input type="checkbox" name="hub-toggle" id="hub-toggle" ${ defaultSource == 'hub' ? '':'checked'}/>
                        </div>
                    </div>
                </div>
                <hr/>
                <div id="searchControls">
                    <div id="sortWidgets" class="row">
                        <div class="col-sm-4">
                            <label for="per-page"><g:message code="public.datasets.sortwidgets.rpp" /></label>
                            <g:select id="per-page" name="per-page" from="${[10,20,50,100,500]}" value="${pageSize ?: 20}"/>
                        </div>

                        <div class="col-sm-4">
                            <label for="sort"><g:message code="public.datasets.sortwidgets.sb" /></label>
                            <g:select id="sort" name="sort" from="${['name','type','license']}"/>
                        </div>

                        <div class="col-sm-4">
                            <label for="dir"><g:message code="public.datasets.sortwidgets.so" /></label>
                            <g:select id="dir" name="dir" from="${['ascending','descending']}"/>
                        </div>
                    </div>
                </div><!--drop downs-->
            </div>

            <div id="loading" class="row-fluid text-center">
                <h3 class="text-primary"><i class="fa fa-spinner fa-spin"></i> <b>Loading...</b></h3>
            </div>
            <div id="results"></div>

            <div id="searchNavBar" class="clearfix">
                <div id="navLinks"></div>
            </div>
        </div>

    </div><!-- close collectory-content-->

</div><!--close content-->

</body>
</html>