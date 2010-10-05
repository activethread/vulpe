<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page" %>
<%@include file="/WEB-INF/protected-jsp/commons/taglibs.jsp" %>
<fmt:setBundle basename="${global['i18nManager']}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta http-equiv="Pragma" content="no-cache"/>
		<meta http-equiv="Cache-Control" content="no-store, no-cache, must-revalidate, post-check=0, pre-check=0"/>
		<c:if test="${global['showAsMobile']}">
		<meta name="viewport" content="width=${global['viewportWidth']}, height=${global['viewportHeight']}, user-scalable=${global['viewportUserScalable']}, initial-scale=${global['viewportInitialScale']}, maximum-scale=${global['viewportMaximumScale']}, minimum-scale=${global['viewportMinimumScale']}" />
		</c:if>
		<fmt:message key="vulpe.frontend.title.application" var="frontendTitle" />
		<title>${not empty vulpePortal ? vulpePortal.title : frontendTitle}</title>
		<link type="image/x-icon" href="${pageContext.request.contextPath}/themes/${global['theme']}/images/icon.png" rel="shortcut icon"/>
		<c:set var="vulpeCurrentLayout" value="FRONTEND" scope="session"/>
		<%@include file="/WEB-INF/protected-jsp/commons/javascript.jsp" %>
		<%@include file="/WEB-INF/protected-jsp/commons/css.jsp" %>
		<decorator:head/>
	</head>
	<body>
		<c:if test="${not empty global['frontendCenteredLayout']}">
			<c:set var="align">align="center"</c:set>
		</c:if>
		<div id="container" ${align}>
			<div id="loading" style="display: none;"></div>
			<div id="modalMessages" style="display: none;" class="vulpeMessages"></div>
			<div id="confirmationDialog" title="<fmt:message key='vulpe.dialog.confirmation.title'/>" style="display: none">
				<p>
					<span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
					<span id="confirmationMessage"></span>
				</p>
			</div>
			<div id="alertDialog" title="<fmt:message key='vulpe.dialog.warning.title'/>" style="display: none;">
				<p>
					<span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 50px 0;"></span>
					<span id="vulpeAlertMessage"></span>
				</p>
			</div>
			<div id="successDialog" title="<fmt:message key='vulpe.dialog.success.title'/>" style="display: none;">
				<p>
					<span class="ui-icon ui-icon-circle-check" style="float:left; margin:0 7px 50px 0;"></span>
					<span id="successMessage"></span>
				</p>
			</div>
			<div id="frontend">
				<div id="header">
					<%@include file="/WEB-INF/protected-jsp/commons/header.jsp" %>
				</div>
				<div id="menu">
					<ul id="nav">
						<%@include file="/WEB-INF/protected-jsp/commons/menu.jsp" %>
					</ul>
				</div>
				<div id="messages" style="display: none;" class="vulpeMessages"></div>
				<div id="body">
					<decorator:body/>
				</div>
			</div>
			<div id="footer">
				<%@include file="/WEB-INF/protected-jsp/commons/footer.jsp" %>
			</div>
		</div>
	</body>
</html>