package au.org.ala.collectory

class CollectoryHubJenkinsService {
    CollectoryHubRestService collectoryHubRestService
    def grailsApplication

    public Map submitParameterizedJob(String jobName, String uid, String dr){
        String url = "${grailsApplication.config.jenkins.url}/job/${jobName}/buildWithParameters?" +
                "token=${grailsApplication.config.jenkins.token}&apikey=${grailsApplication.config.webservice.apiKey}" +
                "&uid=${uid}&dr=${dr}&collectory=${grailsApplication.config.collectory.baseUrl}"
        collectoryHubRestService.doPost(url,"")
    }

    public Map processWithIndexing(String uid, String dr){
        String jobName = grailsApplication.config.jenkins.processWithIndexing
        if(jobName){
            return submitParameterizedJob(jobName, uid, dr)
        } else {
            log.error("Config jenkins.processWithIndexing missing.")
            throw new Exception("Jenkins job name not provided in config.")
        }
    }

    public Map processWithoutIndexing(String uid, String dr){
        String jobName = grailsApplication.config.jenkins.processWithoutIndexing
        if(jobName){
            return submitParameterizedJob(jobName, uid, dr)
        } else {
            log.error("Config jenkins.processWithoutIndexing missing.")
            throw new Exception("Jenkins job name not provided in config.")
        }
    }

    public  Map testRun(){

    }
}
