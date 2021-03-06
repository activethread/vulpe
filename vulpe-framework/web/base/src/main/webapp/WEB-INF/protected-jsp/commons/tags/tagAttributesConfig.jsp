<c:if test="${empty emptyLabel}"><c:set var="emptyLabel" value="label.vulpe.empty"/></c:if>
<c:if test="${empty showEmptyLabel}"><c:set var="showEmptyLabel" value="${true}"/></c:if>
<c:if test="${empty saveInSession}"><c:set var="saveInSession" value="${false}"/></c:if>
<c:if test="${empty expireInSession}"><c:set var="expireInSession" value="${false}"/></c:if>
<c:if test="${empty paragraph}"><c:set var="paragraph" value="true"/></c:if>
<c:if test="${empty style}"><c:set var="style" value=""/></c:if>
<c:if test="${empty show}"><c:set var="show" value="${true}"/></c:if>
<c:if test="${empty focused}"><c:set var="focused" value="true"/></c:if>
<c:set var="styleClass" value="${styleClass}${focused ? ' focused' : ''}"/>
<c:if test="${required && !showAsText}"><c:set var="styleClass" value="${styleClass} vulpeRequired"/></c:if>
<c:if test="${global['application-mobile-enabled'] || global['application-view-breakLabel']}"><c:set var="breakLabel" value="${true}"/></c:if>
<c:if test="${empty targetName}">
	<c:if test="${empty targetConfig}"><c:set var="targetName" value="${not empty now['targetName'] ? now['targetName'] : 'entity'}"/></c:if>
	<c:if test="${not empty targetConfig}"><c:set var="targetName" value="${targetConfigPropertyName}[${currentStatus.index}]"/></c:if>
</c:if>
<c:if test="${empty targetValue}">
	<c:choose>
		<c:when test="${empty targetConfig}">
			<c:set var="targetValueEL" value="${'${'}${targetName}${'}'}"/>
			<c:set var="targetValue" value="${util:eval(pageContext, targetValueEL)}"/>
		</c:when>
		<c:otherwise>
			<c:set var="targetValue" value="${currentItem}"/>
			<c:if test="${empty targetValue && not empty targetName}">
				<c:set var="targetValueEL" value="${'${'}${targetName}${'}'}"/>
				<c:set var="targetValue" value="${util:eval(pageContext, targetValueEL)}"/>
			</c:if>
		</c:otherwise>
	</c:choose>
</c:if>
<c:if test="${not empty property && empty name}"><c:set var="name" value="${targetName}.${property}"/></c:if>
<c:if test="${empty elementId}">
	<c:set var="prepareName" value="${fn:replace(name, '[', '__')}"/>
	<c:set var="prepareName" value="${fn:replace(prepareName, '].', '__')}"/>
	<c:set var="elementId" value="${prepareName}"/>
</c:if>
<c:set var="elementId" value="${fn:replace(elementId, '.', '_')}"/>
<c:if test="${not empty property && empty value}">
	<c:set var="valueEL" value="${'${'}targetValue.${property}${'}'}"/>
	<c:set var="value" value="${util:eval(pageContext, valueEL)}"/>
</c:if>
<c:if test="${saveInSession}"><c:set var="valueInSession" value="${util:saveInSession(name, value, expireInSession)}"/></c:if>
<c:if test="${not empty value}"><c:set var="value" value="${util:toString(value)}"/></c:if>
<c:if test="${empty showAsText && now['onlyToSee'] && targetName != 'entitySelect'}"><c:set var="showAsText" value="${true}"/></c:if>
<c:if test="${onlyUpdateDetails && targetName == 'entity'}"><c:set var="showAsText" value="${true}"/></c:if>
<c:if test="${empty showRequiredIcon}"><c:set var="showRequiredIcon" value="${true}"/></c:if>