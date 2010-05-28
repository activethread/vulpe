<%@include file="/WEB-INF/protected-jsp/commons/taglibs.jsp" %>

<div id="vulpeSelect">
	<div id="vulpeSelectActions" class="vulpeActions">
		<%@include file="/WEB-INF/protected-jsp/commons/selectActions.jsp" %>
	</div>
	<div id="vulpeSelectForm">
		<p class="vulpeSelectForm">
			<jsp:include page="${actionConfig.viewPath}" />
		</p>
	</div>

	<div id="vulpeSelectTable_${actionConfig.formName}">
		<p class="vulpeSelectTable">
			<jsp:include page="${actionConfig.viewItemsPath}" />
			<c:if test="${actionConfig.type == 'REPORT' && not empty downloadInfo}">
				<v:hidden name="downloadInfo" value="${downloadInfo}" saveInSession="true" expireInSession="true" render="false"/>
				<script type="text/javascript">
					$(document).ready(function() {
						vulpe.view.request.submitReport('${util:linkKey('downloadInfo', '', '')}', 800, 600);
					});
				</script>
			</c:if>
		</p>
	</div>

	<div id="vulpeSelectFooter">
		<p class="vulpeSelectFooter">
		</p>
	</div>
</div>