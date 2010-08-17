<c:if test="${!showAsText || !onlyToSee}">
<img id="${elementId}_loading" class="vulpeImageFieldLoading" src="${pageContext.request.contextPath}/themes/${global['theme']}/images/ajax/field-loader.gif" />
<img id="${elementId}_ErrorMessage" class="vulpeImageErrorMessage" src="${pageContext.request.contextPath}/themes/${global['theme']}/images/icons/field-error-stop.png" />
</c:if>
<c:if test="${paragraph}"></p></c:if>
<script type="text/javascript">
vulpe.util.get('${elementId}_ErrorMessage').click(function(){
	vulpe.util.get('${elementId}').focus()
});
<c:if test="${not empty requiredField || not empty validateType}">
<c:if test="${not empty requiredField}">
vulpe.util.get('${elementId}').blur(function() {
	var requiredFieldId = "${vulpeFormName}_${fn:replace(prepareName, property, requiredField)}";
	var value = $(this).val();
	var requiredFieldValue = vulpe.util.get(requiredFieldId).val();
	var id = requiredFieldId + "FieldRequired";
	if (value == "" || requiredFieldValue != "") {
		vulpe.util.get(requiredFieldId).removeClass("vulpeRequired");
		vulpe.util.get(id).hide();
	} else {
		vulpe.util.get(requiredFieldId).addClass("vulpeRequired");
		if (vulpe.util.get(id).length == 0) {
			vulpe.util.get(requiredFieldId + '_ErrorMessage').after("<span id='" + id + "' class='vulpeFieldRequired'>*</span>");
		}
		vulpe.util.get(id).show();
	}
});
</c:if>
<c:if test="${not empty validateType}">
vulpe.config.elements["${elementId}"] = {
	type: '${validateType}'
	<c:if test="${not empty validateRange}">,range: '${validateRange}'</c:if>
	<c:if test="${not empty validateMin}">,min: '${validateMin}'</c:if>
	<c:if test="${not empty validateMax}">,max: '${validateMax}'</c:if>
	<c:if test="${not empty validateMinLength}">,minlength: '${validateMinLength}'</c:if>
	<c:if test="${not empty validateMaxLength}">,maxlength: '${validateMaxLength}'</c:if>
	<c:if test="${not empty validateMask}">,mask: '${validateMask}'</c:if>
	<c:if test="${not empty validateDatePattern}">,datePattern: '${validateDatePattern}'</c:if>
}
</c:if>
</c:if>
</script>