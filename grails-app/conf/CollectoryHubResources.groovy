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
 * Created by Temi on 6/07/2016.
 */
modules = {
    commonStyles{
        resource url: [dir:  'css', file: 'temp-style.css', plugin: 'collectory-hub']
    }

    collectory {
        dependsOn 'commonStyles', 'jquery', 'jquery_i18n', 'jquery_json', 'jquery_tools', 'jquery_jsonp', 'bootstrap2', 'commonStyles'
        resource url: [dir: 'js', file:'collectory.js', plugin: 'collectory-hub']
    }

    bootstrap2 {
        dependsOn 'jquery'
        resource url: [dir: 'bootstrap/js', file: 'bootstrap.js', plugin: 'collectory-hub'], disposition: 'head', exclude: '*'
        resource url: [dir: 'bootstrap/css', file: 'bootstrap.css', plugin: 'collectory-hub'], attrs: [media: 'screen, projection, print']
        resource url: [dir: 'bootstrap/css', file: 'bootstrap-responsive.css', plugin: 'collectory-hub'], attrs: [media: 'screen', id: 'responsiveCss'], exclude: '*'
    }

    jquery_i18n {
        resource url: [dir:'js', file: 'jquery.i18n.properties-1.0.9.min.js', plugin: 'collectory-hub']
    }

    jquery_json {
        resource url: [dir:'js', file:'jquery.json-2.2.min.js', plugin: 'collectory-hub']
    }

    jquery_tools {
        resource url: [dir: 'js', file:'jquery.tools.min.js', plugin: 'collectory-hub']
    }

    jquery_jsonp {
        resource url: [dir: 'js', file:'jquery.jsonp-2.1.4.min.js', plugin: 'collectory-hub']
    }

    bbq {
        resource url: [dir: 'js', file: 'jquery.ba-bbq.min.js', plugin: 'collectory-hub']
    }

    rotate {
        resource url: [ dir:'js', file: 'jQueryRotateCompressed.2.1.js', plugin: 'collectory-hub']
    }

    pagination {
        resource url: [dir:'css', file: 'pagination.css', plugin: 'collectory-hub']
    }

    bootstrapSwitch {
        dependsOn 'bootstrap2', 'jquery'
        resource url: [dir: 'css', file: 'bootstrap-switch.css', plugin: 'collectory-hub']
        resource url: [dir: 'js', file: 'bootstrap-switch.min.js', plugin: 'collectory-hub']
    }

    datasets {
        resource url: [dir:  'js', file: 'datasets.js', plugin: 'collectory-hub']
    }

    fancybox {
        resource url: 'js/third-party/jquery.fancybox/fancybox/jquery.fancybox-1.3.1.css'
        resource url: 'js/third-party/jquery.fancybox/fancybox/jquery.fancybox-1.3.1.pack.js'
    }

    jstree {
        resource url: [dir: 'js/third-party/jstree/', file: 'jquery.jstree.js', plugin: 'collectory-hub']
        resource url: [dir:'js/third-party/themes/classic/', file: 'style.css', plugin: 'collectory-hub'], attrs:[media:'screen, projection, print']
    }

    jquery_ui_custom {
        resource url: [dir: 'js/third-party/jquery-ui/', file: 'jquery-ui-1.11.2-no-autocomplete.js', plugin: 'collectory-hub']
    }

    charts {
        resource url: [ dir:'js/', file: 'charts2.js', plugin: 'collectory-hub']
        resource url: [dir: 'js/', file: 'charts.js', plugin: 'collectory-hub']
    }

    datadumper {
        resource url: [dir: 'js/third-party/datadumper/', file: 'datadumper.js', plugin: 'collectory-hub']
    }

    console {
        resource url: [dir: 'js', file: 'console.js', plugin: 'collectory-hub']
    }
}