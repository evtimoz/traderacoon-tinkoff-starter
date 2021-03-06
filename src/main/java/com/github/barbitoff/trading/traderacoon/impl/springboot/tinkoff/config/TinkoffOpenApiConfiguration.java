package com.github.barbitoff.trading.traderacoon.impl.springboot.tinkoff.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.OpenApiFactoryBase;
import ru.tinkoff.invest.openapi.SandboxOpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Sets up component scan and provides OpenApi bean for access to the underlying
 * Tinkoff API
 */
@Configuration
@EnableConfigurationProperties(TinkoffOpenApiProperties.class)
@ComponentScan("com.github.barbitoff.trading.traderacoon.impl.springboot.tinkoff")
@Slf4j
public class TinkoffOpenApiConfiguration {

    public static final String BEANS_QUALIFIER_PREFIX = "Tinkoff";

    private TinkoffOpenApiProperties props;

    public TinkoffOpenApiConfiguration(TinkoffOpenApiProperties props) {
        this.props = props;
    }

    @Bean(destroyMethod = "close")
    public OpenApi getOpenApi() {
        log.info("Initializing Tinkoff API");
        OpenApi api;
        var factory = getOpenApiFactory();
        if (props.getSandbox().isEnabled()) {
            log.info("Using SANDBOX mode");
            api = factory.createSandboxOpenApiClient(Executors.newSingleThreadExecutor());
            ((SandboxOpenApi) api).getSandboxContext().performRegistration(null).join();
        } else {
            log.info("Using PRODUCTIVE mode");
            api = factory.createOpenApiClient(Executors.newSingleThreadExecutor());
        }
        log.info("Initialization finished");
        return api;
    }

    @Bean
    public OpenApiFactoryBase getOpenApiFactory() {
        return new OkHttpOpenApiFactory(
                props.getApiToken(),
                Logger.getLogger(getClass().getCanonicalName())
        );
    }
}
