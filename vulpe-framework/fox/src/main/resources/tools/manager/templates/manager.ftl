<#include "macros.ftl"/>
<@forAllValidManager ; type, manager>
<@source type="manager" override="${manager.override}" name="${manager.moduleName}.src.main.java.${manager.managerPackageName}.${manager.name}">
package ${manager.managerPackageName};

import org.springframework.stereotype.Service;

import ${manager.daoPackageName}.${manager.entityName}DAO;
<#if !manager.managerSuperclassName??>
import org.vulpe.model.services.manager.impl.VulpeBaseManager;
</#if>
import ${manager.packageName}.${manager.entityName};

/**
 * Manager implementation of ${manager.entityName}
 */
@Service
<#if manager.managerSuperclassName??>
<#if manager.inheritance>
public class ${manager.name} extends ${manager.managerSuperclassName}<${manager.entityName}, ${manager.entityName}DAO<${manager.entityName}>> {
<#else>
public class ${manager.name} extends ${manager.managerSuperclassName}<${manager.entityName}> {
</#if>
<#else>
<#if manager.inheritance>
public class ${manager.name}<ENTITY_CLASS extends ${manager.entityName}, ENTITY_DAO extends ${manager.entityName}DAO<ENTITY_CLASS>> extends VulpeBaseManager<ENTITY_CLASS, ${manager.idType}, ENTITY_DAO> {
<#else>
public class ${manager.name} extends VulpeBaseManager<${manager.entityName}, ${manager.idType}, ${manager.entityName}DAO> {
</#if>
</#if>

}
</@source>
</@forAllValidManager>