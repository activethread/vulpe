<%@include file="/WEB-INF/protected-jsp/commons/actions.jsp"%>
<p>
<%@include file="/WEB-INF/protected-jsp/commons/selectActionsPrepend.jsp"%>
<c:set var="layer" value="body" />
<c:if test="${now['bodyTwice']}"><c:set var="layer" value="select" /></c:if>
<v:action validate="false" layer="${now['popup'] ? now['popupKey'] : layer}" labelKey="clear" elementId="Clear" action="clear" icon="clear" iconClass="Clear" config="${util:buttonConfig(pageContext, 'clear', 'SELECT')}" layerFields="this" />
<c:if test="${!now['onlyToSee']}">
	<v:action validate="false" layer="${now['popup'] ? now['popupKey'] : ''}" labelKey="create" elementId="Create" action="create" beforeJs="vulpe.view.resetFields(%27${vulpeFormName}%27)" layerFields="vulpeSelectForm" helpKey="create" icon="add" iconClass="Create" config="${util:buttonConfig(pageContext, 'create', 'SELECT')}" />
</c:if>
<v:action labelKey="read" elementId="Read" layer="vulpeSelectTable" action="read" beforeJs="vulpe.view.prepareRead(%27${vulpeFormName}%27)" layerFields="vulpeSelectForm" helpKey="read" icon="search" iconClass="Read" config="${util:buttonConfig(pageContext, 'read', 'SELECT')}" />
<v:action labelKey="report" elementId="Report" layer="vulpeSelectTable" action="${now['controllerConfig'].reportControllerName}/report/ajax" helpKey="report" icon="report" iconClass="Report" config="${util:buttonConfig(pageContext, 'report', 'SELECT')}" />
<c:if test="${not empty now['urlBack']}">
	<v:action validate="false" labelKey="back" elementId="Back" action="${now['urlBack']}"	helpKey="back" layerFields="this" icon="back" iconClass="Back" />
</c:if>
<%@include file="/WEB-INF/protected-jsp/commons/selectActionsAppend.jsp"%>
</p>