package br.com.cursoudemy.productapi.config;

import br.com.cursoudemy.productapi.modules.sales.client.SalesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpInterfaceConfig {
    @Value("${app-config.services.sales}")
    private String baseUrl;

    @Bean
    public SalesClient salesClient() {
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(WebClient.builder().baseUrl(this.baseUrl).build())).build().createClient(SalesClient.class);
    }
}
