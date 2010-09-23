<%@include file="/WEB-INF/protected-jsp/commons/taglibs.jsp" %>
<c:set var="vulpeFormName" value="${controllerConfig.formName}" scope="request"/>
<c:if test="${vulpeBodyTwice}">
	<c:choose>
	<c:when test="${vulpeBodyTwiceType == 'CRUD'}">
		<c:set var="vulpeFormName" value="${controllerConfig.CRUDFormName}" scope="request"/>
	</c:when>
	<c:when test="${vulpeBodyTwiceType == 'SELECT'}">
		<c:set var="vulpeFormName" value="${controllerConfig.selectFormName}" scope="request"/>
	</c:when>
	</c:choose>
</c:if>
<c:if test="${empty vulpeShowActions || !vulpeShowActions || vulpeBodySelect}">
<c:set var="vulpeShowActions" value="true" scope="request"/>
<script type="text/javascript" charset="utf-8">
$(document).ready(function() {
	vulpe.util.focusFirst();
<c:if test="${(global['frontendMenuType'] == 'DROPPY' && (now['controllerType'] == 'FRONTEND' || vulpeCurrentLayout == 'FRONTEND')) || (global['backendMenuType'] == 'DROPPY' && (now['controllerType'] == 'BACKEND' || vulpeCurrentLayout == 'BACKEND'))}">
	$("#nav").droppy();
</c:if>
<c:if test="${pageContext.request.locale ne 'en_US'}">
	$.datepicker.setDefaults($.datepicker.regional['${pageContext.request.locale}']);
</c:if>
	if (document.forms['${vulpeFormName}']) {
		vulpe.util.removeHotKeys(["return", "Ctrl+f7", "Ctrl+f8", "Ctrl+f9", "Ctrl+f10", "Ctrl+f12", "Ctrl+del", "Alt+f8", "Alt+Shift+del"]);
	<c:if test="${now['buttons']['create'] || now['buttons']['SELECT_create'] || now['buttons']['CRUD_create']}">
		var buttonCreate = vulpe.util.get("vulpeButtonCreate_${vulpeFormName}");
		if (buttonCreate.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f8",
				command: function() {
					buttonCreate.click();
					return false;
				},
				override: true
			});
		}
	</c:if>
	<c:if test="${now['buttons']['createPost'] || now['buttons']['SELECT_createPost'] || now['buttons']['CRUD_createPost']}">
		var buttonCreatePost = vulpe.util.get("vulpeButtonCreatePost_${vulpeFormName}");
		if (buttonCreatePost.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f10",
				command: function() {
					buttonCreatePost.click();
					return false;
				},
				override: true
			});
		}
	</c:if>
	<c:if test="${now['buttons']['updatePost'] || now['buttons']['SELECT_updatePost'] || now['buttons']['CRUD_updatePost']}">
		var buttonUpdatePost = vulpe.util.get("vulpeButtonUpdatePost_${vulpeFormName}");
		if (buttonUpdatePost.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f10",
				command: function() {
					buttonUpdatePost.click();
					return false;
				},
				override: true
			});
		}
	</c:if>
	<c:if test="${now['buttons']['tabularPost']}">
		var buttonTabularPost = vulpe.util.get("vulpeButtonTabularPost_${vulpeFormName}");
		if (buttonTabularPost.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f10",
				command: function() {
					buttonTabularPost.click();
					return false;
				},
				override: true
			});
		}
	</c:if>
	<c:if test="${now['buttons']['delete'] || now['buttons']['SELECT_delete'] || now['buttons']['CRUD_delete']}">
		var buttonDelete = vulpe.util.get("vulpeButtonDelete_${vulpeFormName}");
		if (buttonDelete.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+del",
				command: function() {
					buttonDelete.click();
					return false;
				}
			});
		}
	</c:if>
	<c:if test="${now['buttons']['prepare'] || now['buttons']['SELECT_prepare'] || now['buttons']['CRUD_prepare']}">
		var buttonPrepare = vulpe.util.get("vulpeButtonPrepare_${vulpeFormName}");
		if (buttonPrepare.attr("onclick")) {
			<c:if test="${now['controllerType'] == 'CRUD'}">
			<c:set var="prepare" value="Ctrl+backspace"/>
			</c:if>
			<c:if test="${now['controllerType'] == 'SELECT'}">
			<c:set var="prepare" value="Alt+Shift+del"/>
			</c:if>
			vulpe.util.addHotKey({
				hotKey: "${prepare}",
				command: function() {
					buttonPrepare.click();
					return false;
				},
				override: true
			});
		}
	</c:if>
	<c:if test="${now['buttons']['tabularFilter']}">
		var buttonTabularFilter = vulpe.util.get("vulpeButtonTabularFilter_${vulpeFormName}");
		if (buttonTabularFilter.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f7",
				command: function() {
					buttonTabularFilter.click();
					return false;
				},
				putSameOnReturnKey: true,
				override: true
			});
		}
	</c:if>
	<c:if test="${now['buttons']['tabularReload']}">
		var buttonTabularReload = vulpe.util.get("vulpeButtonTabularReload_${vulpeFormName}");
		if (buttonTabularReload.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f9",
				command: function() {
					buttonTabularReload.click();
					return false;
				},
				override: true
			});
		}
	</c:if>
	<c:if test="${now['buttons']['read'] || now['buttons']['SELECT_read'] || now['buttons']['CRUD_read']}">
		var buttonRead = vulpe.util.get("vulpeButtonRead_${vulpeFormName}");
		if (buttonRead.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f9",
				command: function() {
					buttonRead.click();
					return false;
				},
				putSameOnReturnKey: true,
				override: true
			});
		}
	</c:if>
	<c:if test="${now['buttons']['report'] || now['buttons']['SELECT_report'] || now['buttons']['CRUD_report']}">
		var buttonReport = vulpe.util.get("vulpeButtonReport_${vulpeFormName}");
		if (buttonReport.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f12",
				command: function() {
					buttonReport.click();
					return false;
				}
			});
		}
	</c:if>
	<c:if test="${now['buttons']['clear'] || now['buttons']['SELECT_clear'] || now['buttons']['CRUD_clear']}">
		var buttonClear = vulpe.util.get("vulpeButtonClear_${vulpeFormName}");
		if (buttonClear.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Alt+Shift+del",
				command: function() {
					buttonClear.click();
					return false;
				}
			});
		}
	</c:if>
	<c:set var="buttonCreate_tabular_EL" value="${'${'}now['buttons']['addDetail${targetConfig.name}']${'}'}"/>
	<c:set var="buttonCreate_tabular" value="${util:eval(pageContext, buttonCreate_tabular_EL)}"/>
	<c:if test="${buttonCreate_tabular}">
		var buttonAddDetail_entities = vulpe.util.get("vulpeButtonAddDetail_entities_${vulpeFormName}");
		if (buttonAddDetail_entities.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Ctrl+f8",
				command: function() {
					buttonAddDetail_entities.click();
					return false;
				},
				override: true
			});
		}
	</c:if>
	<c:if test="${!controllerConfig.simple}">
	<c:forEach items="${controllerConfig.details}" var="detail">
		<c:set var="buttonDetailEL" value="${'${'}now['buttons']['addDetail${detail.baseName}']${'}'}"/>
		<c:set var="buttonDetail" value="${util:eval(pageContext, buttonDetailEL)}"/>
		<c:if test="${buttonDetail}">
		var buttonAddDetail_${detail.baseName} = vulpe.util.get("vulpeButtonAddDetail_${detail.baseName}_${vulpeFormName}");
		if (buttonAddDetail_${detail.baseName}.attr("onclick")) {
			vulpe.util.addHotKey({
				hotKey: "Alt+f8",
				command: function() {
					buttonAddDetail_${detail.baseName}.click();
					return false;
				}
			});
		}
		</c:if>
	</c:forEach>
	</c:if>
		vulpe.util.get('${vulpeFormName}_operation').each(function(){
			$(this).val('${operation}');
			$(this).attr('defaultValue', $(this).val());
		});
		vulpe.util.get('${vulpeFormName}_paging.page').each(function(){
			$(this).val('${paging.page}');
			$(this).attr('defaultValue', $(this).val());
		});
		vulpe.util.get('${vulpeFormName}_id').each(function(){
			$(this).val('${id}');
			$(this).attr('defaultValue', $(this).val());
		});
		vulpe.util.get('${vulpeFormName}_executed').each(function(){
			$(this).val('${executed}');
			$(this).attr('defaultValue', $(this).val());
		});
		<c:if test="${now['controllerType'] != 'FRONTEND' && now['controllerType'] != 'BACKEND'}">
		vulpe.util.get('${vulpeFormName}_entity.orderBy').each(function(){
			$(this).val('${entity.orderBy}');
			$(this).attr('defaultValue', $(this).val());
		});
		</c:if>
	}
	$("#alertDialog").dialog({
		autoOpen: false,
		bgiframe: true,
		modal: true,
		buttons: {
			'<fmt:message key="label.vulpe.button.ok"/>': function() {
				$(this).dialog('close');
			}
		}
	});
	$("#confirmationDialog").dialog({
		autoOpen: false,
		bgiframe: true,
		resizable: false,
		height:140,
		modal: true,
		overlay: {
			backgroundColor: '#000',
			opacity: 0.5
		},
		buttons: {
			'<fmt:message key="label.vulpe.button.ok"/>': function() {
				$(this).dialog('close');
				if (vulpe.command) {
					vulpe.command();
				}
			},
			'<fmt:message key="label.vulpe.button.cancel"/>': function() {
				$(this).dialog('close');
			}
		}
	});
	var columns = jQuery("th[id!='']", "#entities");
	for (var i = 0; i < columns.length; i++) {
		var column = columns[i];
		var order = vulpe.config.order[column.id];
		if (order) {
			vulpe.util.get(order.property).val(order.value);
			column.className = order.css;
		}
	}
	var fields = jQuery("[class*='vulpeRequired']");
	if (fields && fields.length > 0) {
		for (var i = 0; i < fields.length; i++) {
			var field = jQuery(fields[i]);
			var idField = field.attr("id");
			var idRequiredField = idField + "FieldRequired";
			if (vulpe.util.get(idRequiredField).length == 0) {
				vulpe.util.get(idField + "_ErrorMessage").after("<span id='" + idRequiredField + "' class='vulpeFieldRequired'>*</span>");
			}
		}
	}
	<c:choose>
	<c:when test="${now['requireOneFilter']}">vulpe.config.requireOneFilter = true;</c:when>
	<c:otherwise>vulpe.config.requireOneFilter = false;</c:otherwise>
	</c:choose>
});
</script>
</c:if>
<c:if test="${not empty vulpeShowMessages || !vulpeShowMessages}">
<c:set var="vulpeShowMessages" value="true" scope="request"/>
<%@include file="/WEB-INF/protected-jsp/commons/messages.jsp" %>
</c:if>