package br.com.cursoudemy.productapi.modules.sales.rabbitmq;

import br.com.cursoudemy.productapi.modules.sales.dto.SalesConfirmationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SalesConfirmationSender {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    @Value("${app-config.rabbit.exchange.product}")
    private String productTopicExchange;
    @Value("${app-config.rabbit.routingKey.sales-confirmation}")
    private String salesConfirmationKey;

    public void sendSalesConfirmationMessage(SalesConfirmationDTO salesConfirmationDTO) {
        try {
            log.info("Sending message: {}", this.objectMapper.writeValueAsString(salesConfirmationDTO));
            this.rabbitTemplate.convertAndSend(this.productTopicExchange, this.salesConfirmationKey, salesConfirmationDTO);
            log.info("Message was sent successfully!");
        } catch (Exception e) {
            log.info("Error while trying to send sales confirmation message: ", e);
        }
    }
}
