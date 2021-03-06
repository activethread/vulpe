<c:if test="${render}">
	<%@include file="/WEB-INF/protected-jsp/commons/tags/tagBegin.jsp" %>

	<c:if test="${not empty enumeration}">
		<c:set var="enumerationEL" value="${'${'}cachedEnumArray['${enumeration}']${'}'}"/>
		<c:set var="enumeration" value="${util:eval(pageContext, enumerationEL)}"/>
	</c:if>

	<c:if test="${empty styleClass}">
		<c:set var="styleClass" value=".vulpeNoBorder"/>
	</c:if>

	<c:if test="${not empty property && util:isFieldInValidator(targetValue, property)}">
		<c:set var="onblur" value="validate${fn:toUpperCase(fn:substring(property, 0, 1))}${fn:substring(property, 1, -1)}(); ${onblur}"/>
	</c:if>

	<c:if test="${saveInSession}">
		<c:set var="valueInSession" value="${util:saveInSession(name, value, expireInSession)}"/>
	</c:if>

	<c:if test="${not empty property && empty value}">
		<c:set var="valueEL" value="${'${'}targetValue.${property}${'}'}"/>
		<c:set var="value" value="${util:eval(pageContext, valueEL)}"/>
	</c:if>

	<c:if test="${not empty list}">
		<c:set var="listValueCheckEL" value="${'${'}${list}${'}'}"/>
		<c:set var="listValueCheck" value="${util:eval(pageContext, listValueCheckEL)}"/>
	</c:if>

	<span id="${elementId}">
	<c:choose>
		<c:when test="${not empty enumeration || not empty listValueCheck}">
			<c:if test="${now['onlyToSee']}">
				<c:set var="disabled" value="${now['onlyToSee']}" />
			</c:if>
			<c:choose>
			<c:when test="${not empty enumeration}">
				<s:checkboxlist theme="simple" name="${name}" id="${elementId}" accesskey="${accesskey}" disabled="${disabled}" onblur="${onblur}" onchange="${onchange}" onclick="${onclick}" ondblclick="${ondblclick}" onfocus="${onfocus}" onkeydown="${onkeydown}" onkeypress="${onkeypress}" onkeyup="${onkeyup}" onmousedown="${onmousedown}" onmousemove="${onmousemove}" onmouseout="${onmouseout}" onmouseover="${onmouseover}" onmouseup="${onmouseup}" onselect="${onselect}" cssStyle="${style}" cssClass="${styleClass}" tabindex="${tabindex}" title="${title}" list="${enumeration}" />
			</c:when>
			<c:otherwise>
				<s:checkboxlist theme="simple" name="${name}" id="${elementId}" accesskey="${accesskey}" disabled="${disabled}" onblur="${onblur}" onchange="${onchange}" onclick="${onclick}" ondblclick="${ondblclick}" onfocus="${onfocus}" onkeydown="${onkeydown}" onkeypress="${onkeypress}" onkeyup="${onkeyup}" onmousedown="${onmousedown}" onmousemove="${onmousemove}" onmouseout="${onmouseout}" onmouseover="${onmouseover}" onmouseup="${onmouseup}" onselect="${onselect}" cssStyle="${style}" cssClass="${styleClass}" tabindex="${tabindex}" title="${title}" listValue="${listValue}" listKey="${listKey}" list="${list}" />
			</c:otherwise>
			</c:choose>
			<jsp:doBody/>
		</c:when>
		<c:otherwise>
			&nbsp;
		</c:otherwise>
	</c:choose>
	</span>
	<%@include file="/WEB-INF/protected-jsp/commons/tags/tagEnd.jsp" %>
</c:if>