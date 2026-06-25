package com.example.observability.order.service;

import com.example.observability.order.model.PaymentCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentDispatchService {
    private static final Logger log = LoggerFactory.getLogger(PaymentDispatchService.class);

    private final JmsTemplate jmsTemplate;

    public PaymentDispatchService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @NewSpan("order.dispatch-payment")
    public void dispatch(@SpanTag("order.id") PaymentCommand command) {
        jmsTemplate.convertAndSend("checkout.payments", command);
        log.info("payment command dispatched orderId={} customerId={} amountCents={}",
            command.getOrderId(), command.getCustomerId(), command.getAmountCents());
    }
}
