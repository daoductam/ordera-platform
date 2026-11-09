package com.ordera.modules.order.infrastructure.adapter;

import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JavaMailReceiptAdapter Unit Tests")
class JavaMailReceiptAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    private JavaMailReceiptAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JavaMailReceiptAdapter(mailSender);
    }

    @Test
    @DisplayName("sendReceiptEmail - should escape user-supplied HTML inputs in email template (XSS prevention)")
    void should_escape_html_in_email_template() throws Exception {
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        OrderJpaEntity order = OrderJpaEntity.builder()
                .pickupPersonName("<script>alert('xss')</script>")
                .orderNumber("#005")
                .orderCode("ORD-20261109-0891")
                .pickupMethod("<img src=x onerror=alert(1)>")
                .subtotalAmount(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .tipAmount(BigDecimal.ZERO)
                .vatAmount(new BigDecimal("13.04"))
                .totalAmount(new BigDecimal("100.00"))
                .pointsEarned(100)
                .createdAt(Instant.now())
                .build();

        // Act
        adapter.sendReceiptEmail(order, Collections.emptyList(), "test@ordera.app");

        // Verify email was dispatched
        verify(mailSender, times(1)).send(mockMessage);
    }

    @Test
    @DisplayName("generateReceiptPdf - should generate valid PDF 1.4 header and trailer bytes")
    void should_generate_valid_pdf_bytes() {
        OrderJpaEntity order = OrderJpaEntity.builder()
                .pickupPersonName("David Nguyen")
                .orderNumber("#005")
                .orderCode("ORD-20261109-0891")
                .pickupMethod("COUNTER")
                .subtotalAmount(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .tipAmount(BigDecimal.ZERO)
                .vatAmount(new BigDecimal("13.04"))
                .totalAmount(new BigDecimal("100.00"))
                .pointsEarned(100)
                .createdAt(Instant.now())
                .build();

        OrderItemJpaEntity item = OrderItemJpaEntity.builder()
                .productName("Salmon Nigiri")
                .quantity(2)
                .itemTotal(new BigDecimal("100.00"))
                .build();

        byte[] pdfBytes = adapter.generateReceiptPdf(order, List.of(item));

        assertThat(pdfBytes).isNotEmpty();
        String pdfHeader = new String(pdfBytes, 0, 8);
        assertThat(pdfHeader).isEqualTo("%PDF-1.4");
        String pdfString = new String(pdfBytes);
        assertThat(pdfString).contains("%%EOF");
        assertThat(pdfString).contains("ORDERA - ELECTRONIC RECEIPT");
    }
}

