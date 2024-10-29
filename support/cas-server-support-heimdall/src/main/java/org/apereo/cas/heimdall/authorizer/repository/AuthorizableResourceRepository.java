package org.apereo.cas.heimdall.authorizer.repository;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.springframework.beans.factory.DisposableBean;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link AuthorizableResourceRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface AuthorizableResourceRepository extends DisposableBean {
    /**
     * Find authorizable resource.
     *
     * @param request the request
     * @return the authorizable resource
     */
    Optional<AuthorizableResource> find(AuthorizationRequest request);

    /**
     * Find all resources.
     *
     * @return the list of resources
     */
    List<AuthorizableResource> findAll();
    
    @Override
    default void destroy() throws Exception {}
}
