/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.arquillian.container.chameleon;

import org.arquillian.container.chameleon.spi.model.ContainerAdapter;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTestCase {

    @Test
    public void shouldLoadSimpleAdapterConfiguration() throws Exception {
        ChameleonConfiguration configuration = new ChameleonConfiguration();
        configuration.setChameleonTarget("wildfly:8.2.0.Final:managed");
        configuration.validate();

        ContainerAdapter adapter = configuration.getConfiguredAdapter();
        assertThat(adapter.distribution()).isEqualTo("org.wildfly:wildfly-dist:zip:8.2.0.Final");
    }

    @Test
    public void shouldResolveBuildSystemOutputFolderIfDownloadNotSet() throws Exception {
        ChameleonConfiguration configuration = new ChameleonConfiguration();
        assertThat("target").isEqualTo(configuration.getChameleonDistributionDownloadFolder());
    }

    @Test
    public void shouldUseSetDownloadFolder() throws Exception {
        ChameleonConfiguration configuration = new ChameleonConfiguration();
        configuration.setChameleonDistributionDownloadFolder("TEST");
        assertThat("TEST").isEqualTo(configuration.getChameleonDistributionDownloadFolder());
    }

    @Test
    public void shouldSetTempDownloadFolder() throws Exception {
        String tempFolder = "/tmp/";
        System.setProperty("java.io.tmpdir", tempFolder);
        ChameleonConfiguration configuration = new ChameleonConfiguration();
        configuration.setChameleonDistributionDownloadFolder("TMP");
        assertThat(configuration.getChameleonDistributionDownloadFolder().contains(tempFolder + "/arquillian_chameleon")).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnMissingContainerName() throws Exception {
        ChameleonConfiguration configuration = new ChameleonConfiguration();
        configuration.setChameleonTarget("MISSING_TARGET:8.2.0.Final:managed");
        configuration.validate();

        configuration.getConfiguredAdapter();
    }

    @Test(expected = ConfigurationException.class)
    public void shouldFailOnMissingContainerType() throws Exception {
        ChameleonConfiguration configuration = new ChameleonConfiguration();
        configuration.setChameleonTarget("wildfly:8.2.0.Final:UNKNOWN");
        configuration.validate();
    }

    @Test(expected = ConfigurationException.class)
    public void shouldFailOnMissingContainerFile() throws Exception {
        ChameleonConfiguration configuration = new ChameleonConfiguration();
        configuration.setChameleonContainerConfigurationFile("MISSING");
        configuration.setChameleonTarget("wildfly:8.2.0.Final:managed");
        configuration.validate();
    }

    @Test
    public void shouldReturnFalseForMissingContainerName() throws Exception {
        ChameleonConfiguration chameleonConfiguration = new ChameleonConfiguration();
        boolean isSupported = chameleonConfiguration.isSupported("MISSING_TARGET:8.2.0.Final:managed");
        assertThat(isSupported).isFalse();
    }

    @Test
    public void shouldReturnTrueForGivenContainerName() throws Exception {
        ChameleonConfiguration chameleonConfiguration = new ChameleonConfiguration();
        boolean isSupported = chameleonConfiguration.isSupported("wildfly:8.2.0.Final:managed");
        assertThat(isSupported).isTrue();
    }

    @Test
    public void shouldReturnTrueForTomcatContainer() throws Exception {
        ChameleonConfiguration chameleonConfiguration = new ChameleonConfiguration();
        boolean isSupported = chameleonConfiguration.isSupported("Tomcat:7.0.47:Remote");
        assertThat(isSupported).isTrue();
    }

    @Test
    public void shouldReturnFalseForInvalidTomcatContainer() throws Exception {
        ChameleonConfiguration chameleonConfiguration = new ChameleonConfiguration();
        boolean isSupported = chameleonConfiguration.isSupported("Tomcat`:7.0.47:Remote");
        assertThat(isSupported).isFalse();
    }
}
