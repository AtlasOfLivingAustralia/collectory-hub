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
 * Created by Temi on 19/07/2016.
 */
-->
<g:set var="orgNameLong" value="${grailsApplication.config.skin.orgNameLong}"/>
<g:set var="orgNameShort" value="${grailsApplication.config.skin.orgNameShort}"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><g:layoutTitle /></title>
    <style type="text/css">
    body {
        background-color: #ffffff !important;
    }
    #breadcrumb {
        margin-top: 10px;
    }
    #main-content #searchInfoRow #customiseFacetsButton > .dropdown-menu {
        background-color: #ffffff;
    }
    #footer {
        margin: 20px;
        padding-top: 10px;
        border-top: 1px solid #CCC;
        font-size: 12px;
    }
    #content .nav-tabs > li.active > a {
        background-color: #ffffff;
    }
    .nav {
        margin-top: 20px;
    }
    body > #main-content {
        margin-top: 0px;
    }

    </style>
    <r:require modules="bootstrap2, bootstrapSwitch"></r:require>
    <r:layoutResources/>
    <g:layoutHead />
</head>
<body class="${pageProperty(name:'body.class')?:'nav-collections'}" id="${pageProperty(name:'body.id')}" onload="${pageProperty(name:'body.onload')}">
<g:set var="fluidLayout" value="${grailsApplication.config.skin.fluidLayout?.toBoolean()}"/>
<div class="navbar navbar-inverse navbar-static-top">
    <div class="navbar-inner ">
        <div class="${fluidLayout?'container-fluid':'container'}">
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="brand" href="#">${raw(orgNameLong)}</a>
            <div class="nav-collapse collapse">
                <p class="hide navbar-text pull-right">
                    Logged in as <a href="#" class="navbar-link">${username}</a>
                </p>
                <ul class="nav">
                    <li class="active"><a href="#">Home</a></li>
                    <li><a href="#about">About</a></li>
                    <li><a href="#contact">Contact</a></li>
                </ul>
            </div><!--/.nav-collapse -->
        </div><!--/.container-fluid -->
    </div><!--/.navbar-inner -->
</div><!--/.navbar -->


<div class="${fluidLayout?'container-fluid':'container'}" id="main-content">
    <g:layoutBody />
</div><!--/.container-->

<div id="footer">
    <div class="container-fluid">
        <div class="row-fluid">
            <a href="http://creativecommons.org/licenses/by/3.0/au/" title="External link to Creative Commons"><img src="http://i.creativecommons.org/l/by/3.0/88x31.png" width="88" height="31" alt=""></a>
            This site is licensed under a <a href="http://creativecommons.org/licenses/by/3.0/au/" title="External link to Creative Commons" class="external">Creative Commons Attribution 3.0 Australia License</a>.
        Provider content may be covered by other <a href="#terms-of-use" title="Terms of Use">Terms of Use</a>.
        </div>
    </div>
</div><!--/#footer -->
<br/>

<!-- JS resources-->
<r:layoutResources/>
</body>
</html>