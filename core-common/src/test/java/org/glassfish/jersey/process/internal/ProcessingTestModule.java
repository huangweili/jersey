/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.process.internal;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.internal.ContextResolverFactory;
import org.glassfish.jersey.internal.ExceptionMapperFactory;
import org.glassfish.jersey.internal.JaxrsProviders;
import org.glassfish.jersey.internal.ServiceProviders;
import org.glassfish.jersey.internal.ServiceProvidersModule;
import org.glassfish.jersey.internal.inject.AbstractModule;
import org.glassfish.jersey.internal.inject.ContextInjectionResolver;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.MessageBodyFactory;
import org.glassfish.jersey.message.internal.MessagingModules;
import org.glassfish.jersey.message.internal.Responses;
import org.glassfish.jersey.spi.ContextResolvers;
import org.glassfish.jersey.spi.ExceptionMappers;

import org.glassfish.hk2.Services;
import org.glassfish.hk2.inject.Injector;
import org.glassfish.hk2.scopes.Singleton;

import org.jvnet.hk2.annotations.Inject;

/**
 * TODO javadoc.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ProcessingTestModule extends AbstractModule {

    private static final class Refs {

        @Inject
        public ServiceProviders.Builder serviceProvidersBuilder;
        @Inject
        public Ref<ServiceProviders> serviceProviders;
        @Inject
        public Ref<ExceptionMappers> exceptionMappers;
        @Inject
        public Ref<MessageBodyWorkers> messageBodyWorkers;
        @Inject
        public Ref<ContextResolvers> contextResolvers;
    }

    public static void initProviders(final Services services) throws IllegalStateException {
        initProviders(services, Collections.<Class<?>>emptySet(), Collections.<Object>emptySet());
    }

    public static void initProviders(final Services services, final Set<Class<?>> providerClasses, final Set<Object> providerInstances) throws IllegalStateException {
        final Injector injector = services.forContract(Injector.class).get();
        ProcessingTestModule.Refs refs = injector.inject(ProcessingTestModule.Refs.class);

        final ServiceProviders providers = refs.serviceProvidersBuilder
                .setProviderClasses(providerClasses)
                .setProviderInstances(providerInstances)
                .build();
        final MessageBodyWorkers workers = new MessageBodyFactory(providers);
        final ExceptionMappers mappers = new ExceptionMapperFactory(providers);
        final ContextResolvers resolvers = new ContextResolverFactory(providers);

        refs.serviceProviders.set(providers);
        refs.messageBodyWorkers.set(workers);
        refs.exceptionMappers.set(mappers);
        refs.contextResolvers.set(resolvers);
    }

    @Override
    protected void configure() {
        install(
                new RequestScope.Module(),
                new ProcessingModule(),
                new ContextInjectionResolver.Module(),
                new MessagingModules.MessageBodyProviders(),
                new ServiceProvidersModule(Singleton.class),
                new MessageBodyFactory.Module(Singleton.class),
                new ExceptionMapperFactory.Module(Singleton.class),
                new ContextResolverFactory.Module(Singleton.class),
                new JaxrsProviders.Module(),
                new FilterModule());


        bind(ExceptionMapper.class).toInstance(new ExceptionMapper<Throwable>() {

            @Override
            public Response toResponse(Throwable exception) {
                if (exception instanceof NumberFormatException) {
                    return Responses.empty().entity(-1).build();
                }

                throw new RuntimeException(exception);
            }
        });
    }
}
