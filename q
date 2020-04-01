[1mdiff --git a/Realization/backend/core/core-api/src/main/java/eu/bcvsolutions/idm/core/api/config/domain/PrivateIdentityConfiguration.java b/Realization/backend/core/core-api/src/main/java/eu/bcvsolutions/idm/core/api/config/domain/PrivateIdentityConfiguration.java[m
[1mindex bbd529712a..ff9f95862a 100644[m
[1m--- a/Realization/backend/core/core-api/src/main/java/eu/bcvsolutions/idm/core/api/config/domain/PrivateIdentityConfiguration.java[m
[1m+++ b/Realization/backend/core/core-api/src/main/java/eu/bcvsolutions/idm/core/api/config/domain/PrivateIdentityConfiguration.java[m
[36m@@ -16,6 +16,7 @@[m [mpublic interface PrivateIdentityConfiguration extends Configurable {[m
 [m
 	/**[m
 	 * Supports authorization policies for extended form definitions and their values[m
[32m+[m	[32m * @deprecated @since 10.2.0 secured attributes will be supported only[m
 	 */[m
 	String PROPERTY_IDENTITY_FORM_ATTRIBUTES_SECURED = [m
 			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.identity.formAttributes.secured";[m
[36m@@ -54,6 +55,7 @@[m [mpublic interface PrivateIdentityConfiguration extends Configurable {[m
 	 * Returns true, when supports authorization policies for extended form definitions and their values[m
 	 * [m
 	 * @return[m
[32m+[m	[32m * @deprecated @since 10.2.0 secured attributes will be supported only, this configuration will be removed[m
 	 */[m
 	boolean isFormAttributesSecured();[m
 	[m
[1mdiff --git a/Realization/backend/ic/src/main/java/eu/bcvsolutions/idm/ic/service/api/IcConnectorService.java b/Realization/backend/ic/src/main/java/eu/bcvsolutions/idm/ic/service/api/IcConnectorService.java[m
[1mindex b929238a76..1e8eb1f232 100644[m
[1m--- a/Realization/backend/ic/src/main/java/eu/bcvsolutions/idm/ic/service/api/IcConnectorService.java[m
[1m+++ b/Realization/backend/ic/src/main/java/eu/bcvsolutions/idm/ic/service/api/IcConnectorService.java[m
[36m@@ -1,7 +1,6 @@[m
 package eu.bcvsolutions.idm.ic.service.api;[m
 [m
 import java.util.List;[m
[31m-import java.util.Map;[m
 [m
 import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;[m
 import eu.bcvsolutions.idm.ic.api.IcAttribute;[m
[36m@@ -116,6 +115,4 @@[m [mpublic interface IcConnectorService {[m
 	 */[m
 	IcConnector getConnectorInstance(IcConnectorInstance connectorInstance,[m
 			IcConnectorConfiguration connectorConfiguration);[m
[31m-[m
[31m-[m
 }[m
\ No newline at end of file[m
