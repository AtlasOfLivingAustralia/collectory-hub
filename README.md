> **_NOTE:_** Please note that this repo is no longer in user - please refer to [collecotry](https://github.com/AtlasOfLivingAustralia/collectory) instead.
# collectory-hub [![Build Status](https://travis-ci.org/AtlasOfLivingAustralia/collectory-hub.svg?branch=master)](https://travis-ci.org/AtlasOfLivingAustralia/collectory-hub)

**collectory-hub** plugin provides a web UI for a back-end service called [**collectory**](https://github.com/AtlasOfLivingAustralia/collectory-plugin). It aims to replicate the service hub architecture used by many applications in ALA. For example, biocache hub has three components - biocache-service, biocache-hub and ala-hub. Biocache-service handles webservices, biocahe-hub plugin contains all web pages of the application and ala-hub is the front end which uses the necessary pages of biocahe-hub and overides pages.

## Why?
There is a need to share collectory pages with other applications. However, collectory application's front end is tightly coupled with back end. This plugin aims to decouple front end from backend so that collectory pages can be shared with applications that need them. This plugin talks to collectory via webservices.
An example Grails application that uses this plugin, is the [**sandbox**](https://github.com/AtlasOfLivingAustralia/sandbox) app.

## Limitations
Due to time constraints all the pages in the collectory application could not be moved here. Please feel free to add them if it does not already exist.

## Getting started
The easiest way to get started is add **collectory-hub** as a plugin in BuildConfig.groovy. Any functionality that you wish to alter, is achieved by creating a copy of the groovy/GSP/JS/CSS/i18n file of interest, from **collectory-hub** and placing it in your client app, so that it overrides the plugin version.

E.g. to change the header and footer, create a copy of the file `https://github.com/AtlasOfLivingAustralia/ala-bootstrap3/blob/grails3/grails-app/views/layouts/main.gsp` (calling it generic.gsp) and then edit the configuration file to point to this new GSP file: `grails-app/conf/application.groovy` - set `skin.layout = 'generic'`.

A full list of the configuration settings (and their default values) are found in `grails-app/conf/application.groovy`.
