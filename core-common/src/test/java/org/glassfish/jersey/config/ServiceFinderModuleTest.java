/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.config;

import java.util.Collection;
import java.util.Set;

import org.glassfish.jersey.internal.ServiceFinderModule;
import org.glassfish.jersey.internal.inject.AbstractModule;
import org.glassfish.jersey.internal.inject.Providers;

import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Services;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Service finder module unit test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ServiceFinderModuleTest {

    private static Services services;

    public ServiceFinderModuleTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        services = HK2.get().create(null, new AbstractModule() {

            @Override
            protected void configure() {
                bind(TestContract.class).to(TestServiceB.class);
                bind(TestContract.class).to(TestServiceD.class);
                new ServiceFinderModule<TestContract>(TestContract.class).configure(this);
            }
        });
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testConfigure() {
        final Set<TestContract> providers = Providers.getProviders(services, TestContract.class);
        assertEquals(4, providers.size());

        final Collection<String> providerNames = Collections2.transform(providers, new Function<TestContract, String>() {

            @Override
            public String apply(TestContract input) {
                return input.name();
            }
        });

        assertTrue(providerNames.contains(TestServiceA.class.getName()));
        assertTrue(providerNames.contains(TestServiceB.class.getName()));
        assertTrue(providerNames.contains(TestServiceC.class.getName()));
        assertTrue(providerNames.contains(TestServiceD.class.getName()));
    }
}
