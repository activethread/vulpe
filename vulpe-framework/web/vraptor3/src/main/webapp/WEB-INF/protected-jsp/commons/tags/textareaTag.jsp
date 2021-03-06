<c:if test="${render}">
	<%@include file="/WEB-INF/protected-jsp/commons/tags/tagBegin.jsp" %>
	<c:choose>
		<c:when test="${showAsText}">
			<span onclick="${onclick}" ondblclick="${ondblclick}" onkeydown="${onkeydown}" onkeypress="${onkeypress}" onkeyup="${onkeyup}" onmousedown="${onmousedown}" onmousemove="${onmousemove}" onmouseout="${onmouseout}" onmouseover="${onmouseover}" onmouseup="${onmouseup}" style="${style}" class="vulpeSimpleLabel ${styleClass}" id="${elementId}" title="${title}" >${value}</span>
			<v:hidden property="${property}"/>
		</c:when>
		<c:otherwise>
			<textarea name="${name}" <c:if test="${not empty accesskey}">accesskey="${accesskey}"</c:if> cols="${cols}" <c:if test="${disabled}">disabled="${disabled}"</c:if><c:if test="${not empty onblur}"> onblur="${onblur}"</c:if><c:if test="${not empty onchange}"> onchange="${onchange}"</c:if> onclick="${onclick}" ondblclick="${ondblclick}" onfocus="${onfocus}" onkeydown="${onkeydown}" onkeypress="${onkeypress}" onkeyup="${onkeyup}" onmousedown="${onmousedown}" onmousemove="${onmousemove}" onmouseout="${onmouseout}" onmouseover="${onmouseover}" onmouseup="${onmouseup}" id="${elementId}" readonly="${readonly}" rows="${rows}" style="${style}" class="${styleClass}" tabindex="${tabindex}" title="${title}" value="${value}"/>
		</c:otherwise>
	</c:choose>
	<jsp:doBody/>
	<%@include file="/WEB-INF/protected-jsp/commons/tags/tagEnd.jsp" %>
</c:if>