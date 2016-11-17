package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.services.RegisteredService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is {@link MetadataUIUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MetadataUIUtils {
    /**
     * The default entityId parameter name.
     */
    public static final String MDUI_FLOW_PARAMETER_NAME = "mduiContext";

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataUIUtils.class);


    protected MetadataUIUtils() {
    }

    /**
     * Gets SP SSO descriptor.
     *
     * @param entityDescriptor the entity descriptor
     * @return the SP SSO descriptor
     */
    public static SPSSODescriptor getSPSsoDescriptor(final EntityDescriptor entityDescriptor) {
        LOGGER.debug("Locating SP SSO descriptor for SAML2 protocol...");
        SPSSODescriptor spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        if (spssoDescriptor == null) {
            LOGGER.debug("Locating SP SSO descriptor for SAML11 protocol...");
            spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML11P_NS);
        }
        if (spssoDescriptor == null) {
            LOGGER.debug("Locating SP SSO descriptor for SAML1 protocol...");
            spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML10P_NS);
        }
        LOGGER.debug("SP SSO descriptor resolved to be [{}]", spssoDescriptor);
        return spssoDescriptor;
    }

    /**
     * Locate mdui for entity id simple metadata ui info.
     *
     * @param metadataAdapter   the metadata adapter
     * @param entityId          the entity id
     * @param registeredService the registered service
     * @return the simple metadata ui info
     */
    public static SimpleMetadataUIInfo locateMDUIForEntityId(final MetadataResolverAdapter metadataAdapter,
                                         final String entityId,
                                         final RegisteredService registeredService) {
        final EntityDescriptor entityDescriptor = metadataAdapter.getEntityDescriptorForEntityId(entityId);
        return locateMDUIForEntityId(entityDescriptor, entityId, registeredService);
    }

    /**
     * Locate mdui for entity id simple metadata ui info.
     *
     * @param entityDescriptor  the entity descriptor
     * @param entityId          the entity id
     * @param registeredService the registered service
     * @return the simple metadata ui info
     */
    public static SimpleMetadataUIInfo locateMDUIForEntityId(final EntityDescriptor entityDescriptor,
                                                             final String entityId,
                                                             final RegisteredService registeredService) {
        if (entityDescriptor == null) {
            LOGGER.debug("Entity descriptor not found for [{}]", entityId);
            return null;
        }

        final SPSSODescriptor spssoDescriptor = MetadataUIUtils.getSPSsoDescriptor(entityDescriptor);
        if (spssoDescriptor == null) {
            LOGGER.debug("SP SSO descriptor not found for [{}]", entityId);
            return null;
        }

        final Extensions extensions = spssoDescriptor.getExtensions();
        if (extensions == null) {
            LOGGER.debug("No extensions are found for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return null;
        }

        final List<XMLObject> spExtensions = extensions.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME);
        if (spExtensions.isEmpty()) {
            LOGGER.debug("No extensions are located for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return null;
        }

        final SimpleMetadataUIInfo mdui = new SimpleMetadataUIInfo(registeredService);
        spExtensions.stream().filter(obj -> obj instanceof UIInfo).forEach(obj -> {
            final UIInfo uiInfo = (UIInfo) obj;
            LOGGER.debug("Found UI info for [{}] and added to flow context", entityId);
            mdui.setUIInfo(uiInfo);
        });
        return mdui;
    }
}
