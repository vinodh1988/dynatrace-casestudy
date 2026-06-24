package com.example.observability.order.config;

import com.example.observability.order.jmx.OrderStats;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JmxConfig {
    public JmxConfig(OrderStats orderStats) throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("checkout.case.study:type=OrderStats,name=OrderApi");
        if (!server.isRegistered(name)) {
            server.registerMBean(orderStats, name);
        }
    }
}
