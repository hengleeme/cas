package org.apereo.cas.support.x509.rest.config;

import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreRestAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasRestConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowContextConfiguration;
import org.apereo.cas.config.X509CertificateExtractorConfiguration;
import org.apereo.cas.config.X509RestConfiguration;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509RestConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    X509CertificateExtractorConfiguration.class,
    X509RestConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreConfiguration.class,
    CasThrottlingConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreRestAutoConfiguration.class,
    CasRestConfiguration.class
},
    properties = "cas.rest.x509.tls-client-auth=true")
@Tag("X509")
class X509RestConfigurationTests {
    @Autowired
    @Qualifier("x509RestMultipartBody")
    private RestHttpRequestCredentialFactory x509RestMultipartBody;

    @Autowired
    @Qualifier("x509RestRequestHeader")
    private RestHttpRequestCredentialFactory x509RestRequestHeader;

    @Autowired
    @Qualifier("x509RestTlsClientCert")
    private RestHttpRequestCredentialFactory x509RestTlsClientCert;
    
    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(x509RestMultipartBody);
        assertNotNull(x509RestRequestHeader);
        assertNotNull(x509RestTlsClientCert);
    }
}
