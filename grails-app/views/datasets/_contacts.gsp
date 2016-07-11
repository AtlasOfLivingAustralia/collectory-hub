<g:if test="${contacts?.size() > 0}">
  <div class="section">
    <h3>Contact</h3>
    <g:each in="${contacts}" var="cf">
      <div class="contact">
        <p>
            <span class="contactName">${collectoryService.buildName(cf?.contact)}</span><br/>
            <g:if test="${cf?.role}">${cf?.role}<br/></g:if>
            <g:if test="${cf?.contact?.phone}">phone: ${cf?.contact?.phone}<br/></g:if>
            <g:if test="${cf?.contact?.fax}">phone: ${cf?.contact?.fax}<br/></g:if>
            <ch:emailLink email="${cf?.contact?.email}">email this contact</ch:emailLink>
        </p>
      </div>
    </g:each>
  </div>
</g:if>
