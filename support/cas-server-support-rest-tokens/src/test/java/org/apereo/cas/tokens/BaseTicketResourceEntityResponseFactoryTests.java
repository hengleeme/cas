package org.apereo.cas.tokens;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
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
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasRestConfiguration;
import org.apereo.cas.config.CasRestTokensConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowContextConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.List;

/**
 * This is {@link BaseTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    BaseTicketResourceEntityResponseFactoryTests.TicketResourceTestConfiguration.class,
    CasCoreRestAutoConfiguration.class,
    CasRestTokensConfiguration.class,
    CasRestConfiguration.class,
    TokenCoreConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreTicketsConfiguration.class
})
public abstract class BaseTicketResourceEntityResponseFactoryTests {
    @Autowired
    @Qualifier("ticketGrantingTicketResourceEntityResponseFactory")
    protected TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    protected AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("tokenCipherExecutor")
    protected CipherExecutor tokenCipherExecutor;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("serviceTicketResourceEntityResponseFactory")
    protected ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory;

    @TestConfiguration(value = "TicketResourceTestConfiguration", proxyBeanMethods = false)
    static class TicketResourceTestConfiguration implements InitializingBean {

        @Autowired
        @Qualifier("inMemoryRegisteredServices")
        private List<RegisteredService> inMemoryRegisteredServices;

        public void init() {
            inMemoryRegisteredServices.add(RegisteredServiceTestUtils.getRegisteredService("https://cas.example.org.+"));
        }

        @Override
        public void afterPropertiesSet() {
            init();
        }
    }
}
