package com.protocol.supplychainx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SupplyChainXApplicationTests {

    /**
     * Mock JavaMailSender to avoid requiring SMTP configuration in tests.
     * This allows EmailService and dependent beans (like LowStockAlertScheduler)
     * to be created without connecting to a real mail server.
     */
    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void contextLoads() {
    }

}
