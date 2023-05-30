package org.apereo.cas.nativex;

import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.AbstractCasExpirationPolicy;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.pubsub.QueueableTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.core.DecoratingProxy;

/**
 * This is {@link CasCoreTicketsRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreTicketsRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.proxies()
            .registerJdkProxy(TicketSerializationExecutionPlanConfigurer.class)
            .registerJdkProxy(TicketFactoryExecutionPlanConfigurer.class)
            .registerJdkProxy(QueueableTicketRegistry.class, TicketRegistry.class,
                SpringProxy.class, Advised.class, DecoratingProxy.class);
        
        hints.reflection()
            .registerType(TicketGrantingCookieCipherExecutor.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
                MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS);

        hints.serialization()
            .registerType(AbstractTicket.class)
            .registerType(TicketGrantingTicketImpl.class)
            .registerType(ServiceTicketImpl.class)
            .registerType(ProxyGrantingTicketImpl.class)
            .registerType(ProxyTicketImpl.class)
            .registerType(TransientSessionTicketImpl.class)
            
            .registerType(AbstractCasExpirationPolicy.class)
            .registerType(AlwaysExpiresExpirationPolicy.class)
            .registerType(NeverExpiresExpirationPolicy.class)
            .registerType(ThrottledUseAndTimeoutExpirationPolicy.class)
            .registerType(TicketGrantingTicketExpirationPolicy.class)
            .registerType(TimeoutExpirationPolicy.class)
            .registerType(BaseDelegatingExpirationPolicy.class)
            .registerType(HardTimeoutExpirationPolicy.class)
            .registerType(MultiTimeUseOrTimeoutExpirationPolicy.class);
    }
}