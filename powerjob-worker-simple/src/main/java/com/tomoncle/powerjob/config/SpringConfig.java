/*
 * Copyright 2018 tomoncle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomoncle.powerjob.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tomoncle
 */

@Data
@ConfigurationProperties("system.initial")
class SpringConfigEnvProperties {
    private Map<String, String> properties = new HashMap<>();
}

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@EnableConfigurationProperties(SpringConfigEnvProperties.class)
@Slf4j
class SpringConfigEnvLoader {
    private final SpringConfigEnvProperties springConfigEnvProperties;

    public SpringConfigEnvLoader(SpringConfigEnvProperties springConfigEnvProperties) {
        log.debug("Initializing custom configuration to environment variables.");
        this.springConfigEnvProperties = springConfigEnvProperties;
        this.initializeProperties();
    }

    private void initializeProperties() {
        Map<String, String> properties = springConfigEnvProperties.getProperties();
        properties.keySet().stream().filter(key -> null != properties.get(key)).forEachOrdered(key -> {
            if (null != System.getProperty(key)) {
                log.warn("Custom environment variable {} overwrite the default environment variable value.", key);
            }
            System.setProperty(key, properties.get(key));
        });

    }
}
