<!DOCTYPE html>
<html>
<head>
    <title><g:if env="development">Grails Runtime Exception</g:if><g:else>Error</g:else></title>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
</head>
<body>
    <h2>Error</h2>
    <p>An error has occurred.</p>
    <br>
    <h4>Quick links</h4>
    <ol>
        <li><a href="${createLink(uri: '/')}">Home</a></li>
        <li><a href="${createLink(controller: 'tempDataResource', action: 'myData',)}">My datasets</a></li>
    </ol>
</body>
</html>

