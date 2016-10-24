package au.org.ala.collectory

import grails.converters.JSON
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod


class BiocacheRestService {

    def grailsApplication


    def getCustomIndexes(String uid){
        def http = new HttpClient()
        def get = new GetMethod(grailsApplication.config.biocacheServiceUrl + "/upload/customIndexes/${uid}.json")
        http.executeMethod(get)
        JSON.parse(get.getResponseBodyAsString())
    }

    def saveChartOptions(String uid, options){
        def http = new HttpClient()
        def post = new PostMethod(grailsApplication.config.biocacheServiceUrl + "/upload/charts/${uid}")
        post.setRequestBody((options as JSON).toString())
        int status = http.executeMethod(post)
        status
    }

    def getChartOptions(String uid){
        def http = new HttpClient()
        def get = new GetMethod(grailsApplication.config.biocacheServiceUrl + "/upload/charts/${uid}")
        http.executeMethod(get)
        JSON.parse(get.getResponseBodyAsString())
    }
}
