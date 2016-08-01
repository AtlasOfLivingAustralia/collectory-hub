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
        dependsOn 'commonStyles', 'jquery', 'jquery_i18n', 'jquery_json', 'jquery_tools', 'jquery_jsonp', 'bootstrap2'
        resource url: 'js/collectory.js'
    }

    bootstrap2 {
        dependsOn 'jquery'
        resource url: [dir: 'bootstrap/js', file: 'bootstrap.js', plugin: 'collectory-hub'], disposition: 'head', exclude: '*'
        resource url: [dir: 'bootstrap/css', file: 'bootstrap.css', plugin: 'collectory-hub'], attrs: [media: 'screen, projection, print']
        resource url: [dir: 'bootstrap/css', file: 'bootstrap-responsive.css', plugin: 'collectory-hub'], attrs: [media: 'screen', id: 'responsiveCss'], exclude: '*'
    }

    jquery_i18n {
        resource url: 'js/jquery.i18n.properties-1.0.9.min.js'
    }

    jquery_json {
        resource url: 'js/jquery.json-2.2.min.js'
    }

    jquery_tools {
        resource url: 'js/jquery.tools.min.js'
    }

    jquery_jsonp {
        resource url: 'js/jquery.jsonp-2.1.4.min.js'
    }

    bbq {
        resource url: [dir: 'js', file: 'jquery.ba-bbq.min.js']
    }

    rotate {
        resource url: 'js/jQueryRotateCompressed.2.1.js'
    }

    pagination {
        resource url: 'css/pagination.css'
    }

    bootstrapSwitch {
        dependsOn 'bootstrap2', 'jquery'
        resource url: [dir: 'css', file: 'bootstrap-switch.css']
        resource url: [dir: 'js', file: 'bootstrap-switch.min.js']
    }

    datasets {
        resource url: 'js/datasets.js'
    }

    fancybox {
        resource url: 'js/third-party/jquery.fancybox/fancybox/jquery.fancybox-1.3.1.css'
        resource url: 'js/third-party/jquery.fancybox/fancybox/jquery.fancybox-1.3.1.pack.js'
    }

    jstree {
        resource url: 'js/third-party/jstree/jquery.jstree.js'
        resource url: 'js/third-party/themes/classic/style.css', attrs:[media:'screen, projection, print']
    }

    jquery_ui_custom {
        resource url: 'js/third-party/jquery-ui/jquery-ui-1.11.2-no-autocomplete.js'
    }

    charts {
        resource url:'js/charts2.js'
        resource url:'js/charts.js'
    }

    datadumper {
        resource url: 'js/third-party/datadumper/datadumper.js'
    }

    console {
        resource url: [ dir: 'js', file: 'console.js', plugin: 'collectory-hub']
    }
}