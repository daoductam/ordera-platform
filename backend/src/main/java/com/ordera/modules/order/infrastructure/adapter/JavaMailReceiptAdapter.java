package com.ordera.modules.order.infrastructure.adapter;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adapter sinh hóa đơn điện tử PDF và gửi Email thông qua JavaMailSender.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JavaMailReceiptAdapter {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("UTC"));

    /**
     * Sinh nội dung PDF hóa đơn điện tử chuẩn và gửi tới Email khách hàng.
     */
    public void sendReceiptEmail(OrderJpaEntity order, List<OrderItemJpaEntity> items, String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("Cannot send e-receipt: recipient email is null or blank for order {}", order.getOrderCode());
            return;
        }

        try {
            byte[] pdfBytes = generateReceiptPdf(order, items);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(recipientEmail);
            helper.setSubject("Ordera E-Receipt — " + order.getOrderNumber() + " (" + order.getOrderCode() + ")");

            String htmlContent = buildEmailHtml(order, items);
            helper.setText(htmlContent, true);

            String attachmentFilename = "receipt-" + order.getOrderCode() + ".pdf";
            helper.addAttachment(attachmentFilename, new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
            log.info("Dispatched E-Receipt PDF email for order {} to {}", order.getOrderCode(), recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send E-Receipt email to {}: {}", recipientEmail, e.getMessage());
            throw new BusinessException(OrderErrorCode.RECEIPT_EMAIL_DISPATCH_FAILED,
                    "Không thể gửi biên nhận điện tử qua Email: " + e.getMessage());
        }
    }

    /**
     * Sinh file PDF 1.4 chuẩn dạng byte array (không cần thư viện bên thứ 3).
     */
    public byte[] generateReceiptPdf(OrderJpaEntity order, List<OrderItemJpaEntity> items) {
        StringBuilder streamContent = new StringBuilder();
        streamContent.append("BT\n/F1 18 Tf\n50 750 Td\n(ORDERA - ELECTRONIC RECEIPT) Tj\nET\n");
        streamContent.append("BT\n/F1 12 Tf\n50 720 Td\n(Order: ").append(escapePdf(order.getOrderNumber()))
                .append("   Code: ").append(escapePdf(order.getOrderCode())).append(") Tj\nET\n");
        streamContent.append("BT\n/F1 10 Tf\n50 700 Td\n(Customer: ").append(escapePdf(order.getPickupPersonName()))
                .append("   Phone: ").append(escapePdf(order.getPickupPersonPhone())).append(") Tj\nET\n");
        streamContent.append("BT\n/F1 10 Tf\n50 685 Td\n(Method: ").append(escapePdf(order.getPickupMethod()))
                .append("   Date: ").append(DATE_FORMATTER.format(order.getCreatedAt())).append(") Tj\nET\n");

        int y = 650;
        streamContent.append("BT\n/F1 11 Tf\n50 ").append(y).append(" Td\n(--------------------------------------------------------) Tj\nET\n");
        y -= 20;

        if (items != null) {
            for (OrderItemJpaEntity item : items) {
                String line = String.format("%d x %s - %.2f NOK", item.getQuantity(), item.getProductName(), item.getItemTotal());
                streamContent.append("BT\n/F1 10 Tf\n50 ").append(y).append(" Td\n(").append(escapePdf(line)).append(") Tj\nET\n");
                y -= 18;
            }
        }

        y -= 10;
        streamContent.append("BT\n/F1 11 Tf\n50 ").append(y).append(" Td\n(--------------------------------------------------------) Tj\nET\n");
        y -= 20;

        streamContent.append("BT\n/F1 10 Tf\n50 ").append(y).append(" Td\n(Subtotal: ").append(order.getSubtotalAmount()).append(" NOK) Tj\nET\n");
        y -= 16;
        streamContent.append("BT\n/F1 10 Tf\n50 ").append(y).append(" Td\n(Discount: -").append(order.getDiscountAmount()).append(" NOK) Tj\nET\n");
        y -= 16;
        streamContent.append("BT\n/F1 10 Tf\n50 ").append(y).append(" Td\n(Tip: +").append(order.getTipAmount()).append(" NOK) Tj\nET\n");
        y -= 16;
        streamContent.append("BT\n/F1 10 Tf\n50 ").append(y).append(" Td\n(VAT 15% Included: ").append(order.getVatAmount()).append(" NOK) Tj\nET\n");
        y -= 20;
        streamContent.append("BT\n/F1 14 Tf\n50 ").append(y).append(" Td\n(TOTAL PAID: ").append(order.getTotalAmount()).append(" NOK) Tj\nET\n");
        y -= 25;
        streamContent.append("BT\n/F1 10 Tf\n50 ").append(y).append(" Td\n(Points Earned: +").append(order.getPointsEarned()).append(" pts) Tj\nET\n");

        byte[] streamBytes = streamContent.toString().getBytes(StandardCharsets.US_ASCII);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
            int offset1 = out.size();
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));
            int offset2 = out.size();
            out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));
            int offset3 = out.size();
            out.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));
            int offset4 = out.size();
            out.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));
            int offset5 = out.size();
            out.write(String.format("5 0 obj\n<< /Length %d >>\nstream\n", streamBytes.length).getBytes(StandardCharsets.US_ASCII));
            out.write(streamBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            int xrefOffset = out.size();
            out.write("xref\n0 6\n0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            out.write(String.format("%010d 00000 n \n", offset1).getBytes(StandardCharsets.US_ASCII));
            out.write(String.format("%010d 00000 n \n", offset2).getBytes(StandardCharsets.US_ASCII));
            out.write(String.format("%010d 00000 n \n", offset3).getBytes(StandardCharsets.US_ASCII));
            out.write(String.format("%010d 00000 n \n", offset4).getBytes(StandardCharsets.US_ASCII));
            out.write(String.format("%010d 00000 n \n", offset5).getBytes(StandardCharsets.US_ASCII));

            out.write(String.format("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n", xrefOffset).getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            log.error("Error creating PDF bytes: {}", e.getMessage());
        }

        return out.toByteArray();
    }

    private String escapePdf(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String buildEmailHtml(OrderJpaEntity order, List<OrderItemJpaEntity> items) {
        String safeCustomerName = HtmlUtils.htmlEscape(order.getPickupPersonName() != null ? order.getPickupPersonName() : "Customer");
        String safeOrderNumber = HtmlUtils.htmlEscape(order.getOrderNumber() != null ? order.getOrderNumber() : "");
        String safeOrderCode = HtmlUtils.htmlEscape(order.getOrderCode() != null ? order.getOrderCode() : "");
        String safePickupMethod = HtmlUtils.htmlEscape(order.getPickupMethod() != null ? order.getPickupMethod() : "");

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>");
        sb.append("<h2 style='color: #1a1a1a;'>Ordera — Electronic Receipt</h2>");
        sb.append("<p>Dear <strong>").append(safeCustomerName).append("</strong>,</p>");
        sb.append("<p>Thank you for your order at Ordera. Here is your digital receipt:</p>");
        sb.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 6px; margin: 15px 0;'>");
        sb.append("<p><strong>Order Number:</strong> <span style='font-size: 18px; color: #d32f2f;'>").append(safeOrderNumber).append("</span></p>");
        sb.append("<p><strong>Order Code:</strong> ").append(safeOrderCode).append("</p>");
        sb.append("<p><strong>Pickup Method:</strong> ").append(safePickupMethod).append("</p>");
        sb.append("<p><strong>Total Paid:</strong> <strong>").append(order.getTotalAmount()).append(" NOK</strong></p>");
        sb.append("<p><strong>VAT 15% Included:</strong> ").append(order.getVatAmount()).append(" NOK</p>");
        sb.append("<p><strong>Points Earned:</strong> +").append(order.getPointsEarned()).append(" pts</p>");
        sb.append("</div>");
        sb.append("<p>The detailed PDF receipt has been attached to this email.</p>");
        sb.append("<p style='color: #777; font-size: 12px;'>Ordera Platform — Clean Dining & Loyalty Experience</p>");
        sb.append("</div>");
        return sb.toString();
    }
}

