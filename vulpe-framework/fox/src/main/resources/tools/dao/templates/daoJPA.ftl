<#include "macros.ftl"/>
<@forAllValidDAO ; type, dao>
<@source type="dao-impl" override="${dao.override}" name="${dao.daoPackageName}.impl.jpa.${dao.daoName}JPA">
package ${dao.daoPackageName}.impl.jpa;

import ${dao.packageName}.${dao.name};
import ${dao.daoPackageName}.${dao.daoName};

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO JPA implementation of ${dao.name}
 */
@Repository("${dao.daoName}")
@Transactional
<#if dao.methods?has_content>
@SuppressWarnings("unchecked")
</#if>
<#if dao.daoSuperclassName??>
<#if dao.inheritance>
public class ${dao.daoName}JPA<ENTITY_CLASS extends ${dao.name}> extends ${dao.daoPackageName}.impl.jpa.${dao.daoSuperclassSimpleName}JPA<ENTITY_CLASS> implements ${dao.daoName}<ENTITY_CLASS> {
<#else>
public class ${dao.daoName}JPA extends ${dao.daoPackageName}.impl.jpa.${dao.daoSuperclassSimpleName}JPA<${dao.name}> implements ${dao.daoName} {
</#if>
<#else>
<#if dao.inheritance>
public class ${dao.daoName}JPA<ENTITY_CLASS extends ${dao.name}> extends org.vulpe.model.dao.impl.jpa.VulpeBaseDAOJPA<ENTITY_CLASS, ${dao.idType}> implements ${dao.daoName}<ENTITY_CLASS> {
<#else>
public class ${dao.daoName}JPA extends org.vulpe.model.dao.impl.jpa.VulpeBaseDAOJPA<${dao.name}, ${dao.idType}> implements ${dao.daoName} {
</#if>
</#if>
	<#list dao.methods as method>
	public ${method.returnType} ${method.name}(<#list method.parameters as parameter><#if parameter.name != "limit">final ${parameter.type} ${parameter.name}<#if parameter_has_next>, </#if></#if></#list>) throws org.vulpe.exception.VulpeApplicationException {
		<#if method.parameters?has_content>
		final java.util.Map<String, Object> map = new java.util.HashMap();
		<#list method.parameters as parameter>
		<#if parameter.name == "limit">map.put("${parameter.name}", ${parameter.value});<#else>map.put("${parameter.name}", ${parameter.name});</#if>
		</#list>
		</#if>
		<#if method.returnType == dao.name || method.returnType?index_of("List") == -1>
		<#if method.parameters?has_content>
		<#if method.returnType == dao.name>
		final ${method.returnType} object = (${method.returnType}) findByNamedQueryAndNamedParams("${dao.name}.${method.name}", map);
		object.setQueryConfigurationName("${method.name}");
		loadEntityRelationships(object);
		return object;
		<#else>
		return (${method.returnType}) findByNamedQueryAndNamedParams("${dao.name}.${method.name}", map);
		</#if>
		<#else>
		final ${method.returnType} object = (${method.returnType}) findByNamedQuery("${dao.name}.${method.name}");
		<#if method.returnType == dao.name>
		object.setQueryConfigurationName("${method.name}");
		loadEntityRelationships(object);
		</#if>
		return object;
		</#if>
		<#else>
		<#if method.parameters?has_content>
		final ${method.returnType} list = (${method.returnType}) listByNamedQueryAndNamedParams("${dao.name}.${method.name}", map);
		<#else>
		final ${method.returnType} list = (${method.returnType}) listByNamedQuery("${dao.name}.${method.name}");
		</#if>
		<#if method.returnType?index_of(dao.name) != -1>
		if (list != null && !list.isEmpty()) {
			list.get(0).setQueryConfigurationName("${method.name}");
		}
		<#if method.parameters?has_content>
		loadRelationships(list, map, false);
		<#else>
		loadRelationships(list, null, false);
		</#if>
		</#if>
		return list;
		</#if>
	}
	</#list>
}</@source></@forAllValidDAO>