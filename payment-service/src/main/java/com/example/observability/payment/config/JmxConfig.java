package com.example.observability.payment.config;

import com.example.observability.payment.jmx.PaymentStats;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JmxConfig {
    public JmxConfig(PaymentStats paymentStats) throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("checkout.case.study:type=PaymentStats,name=PaymentService");
        if (!server.isRegistered(name)) {
            server.registerMBean(paymentStats, name);
        }
    }
}
