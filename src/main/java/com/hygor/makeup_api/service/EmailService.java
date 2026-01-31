package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.sender}")
    private String senderEmail;

    @Value("${app.mail.admin}")
    private String adminEmail;

    /**
     * Envia e-mail de confirmação de pedido (Async para não travar o checkout).
     * Renderiza o template 'order-confirmation.html'.
     */
    @Async
    public void sendOrderConfirmation(OrderResponse order) {
        log.info("Iniciando envio de e-mail de confirmação para o pedido: {}", order.getOrderNumber());

        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("customerName", extractFirstName(order.getUserEmail())); // Idealmente o DTO teria o nome do
                                                                                     // cliente
        context.setVariable("totalAmount", order.getTotalAmount()); // Garantia de acesso fácil no template

        try {
            String htmlContent = templateEngine.process("email/order-confirmation", context);
            sendHtmlEmail(order.getUserEmail(), "Confirmação do Pedido #" + order.getOrderNumber(), htmlContent);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de confirmação do pedido {}: {}", order.getOrderNumber(), e.getMessage());
        }
    }

    /**
     * Envia e-mail de atualização de status (Pagamento aprovado, Enviado, etc.).
     * Renderiza o template 'status-update.html'.
     */
    @Async
    public void sendOrderStatusUpdate(OrderResponse order) {
        log.info("Enviando atualização de status para pedido: {}", order.getOrderNumber());

        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("status", translateStatus(order.getStatus().toString())); // Traduz para PT-BR se necessário
        context.setVariable("customerName", extractFirstName(order.getUserEmail()));

        String subject = "Atualização do Pedido #" + order.getOrderNumber();

        try {
            String htmlContent = templateEngine.process("email/status-update", context);
            sendHtmlEmail(order.getUserEmail(), subject, htmlContent);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de atualização de status para pedido {}: {}", order.getOrderNumber(),
                    e.getMessage());
        }
    }

    /**
     * Envia token de recuperação de senha.
     * Renderiza o template 'password-recovery.html'.
     */
    @Async
    public void sendRecoveryEmail(User user, String token) {
        log.info("Enviando token de recuperação para: {}", user.getEmail());

        Context context = new Context();
        context.setVariable("name", user.getFirstName());
        context.setVariable("token", token);

        try {
            String htmlContent = templateEngine.process("email/password-recovery", context);
            sendHtmlEmail(user.getEmail(), "Recuperação de Senha - Boutique Hygor & Ana", htmlContent);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de recuperação para {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendLowStockAlert(String productName, String sku, Integer remainingStock) {
        log.warn("STOCK CRÍTICO: {} (SKU: {}) restam apenas {}", productName, sku, remainingStock);

        Context context = new Context();
        context.setVariable("productName", productName);
        context.setVariable("sku", sku);
        context.setVariable("stock", remainingStock);

        try {
            // Crie um template simples 'email/stock-alert.html'
            String html = templateEngine.process("email/stock-alert", context);
            sendHtmlEmail(adminEmail, "ALERTA DE STOCK BAIXO: " + sku, html);
        } catch (Exception e) {
            log.error("Erro ao enviar alerta de stock", e);
        }
    }

    @Async
    public void sendDailyReport(String to, com.hygor.makeup_api.dto.admin.DashboardResponse stats,
            java.time.LocalDate date) {
        log.info("Enviando relatório diário para: {}", to);

        Context context = new Context();
        context.setVariable("date", date);
        context.setVariable("stats", stats);

        // Formatação monetária simples para o template
        context.setVariable("revenue", stats.getTotalRevenue());
        context.setVariable("ticket", stats.getAverageTicket());

        try {
            String htmlContent = templateEngine.process("email/daily-report", context);
            sendHtmlEmail(to, "Relatório Diário - " + date.toString(), htmlContent);
        } catch (Exception e) {
            log.error("Erro ao enviar relatório diário: {}", e.getMessage());
        }
    }

    /**
     * Método genérico para envio de e-mail HTML.
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        // MULTIPART_MODE_MIXED_RELATED permite imagens inline e anexos se necessário
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indica que o conteúdo é HTML

        mailSender.send(message);
        log.info("E-mail enviado com sucesso para: {}", to);
    }

    /**
     * Auxiliar para extrair o primeiro nome do e-mail caso não tenhamos o nome
     * completo.
     * (Fallback simples, idealmente usar o nome do User se disponível)
     */
    private String extractFirstName(String email) {
        if (email == null || !email.contains("@"))
            return "Cliente";
        String namePart = email.split("@")[0];
        // Capitaliza a primeira letra
        return namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
    }

    /**
     * Auxiliar para traduzir status do Enum para texto amigável (Opcional).
     */
    private String translateStatus(String statusEnum) {
        switch (statusEnum) {
            case "WAITING_PAYMENT":
                return "Aguardando Pagamento";
            case "PAID":
                return "Pago";
            case "APPROVED":
                return "Pagamento Aprovado";
            case "PROCESSING":
                return "Em Processamento";
            case "SHIPPED":
                return "Enviado";
            case "DELIVERED":
                return "Entregue";
            case "CANCELLED":
                return "Cancelado";
            default:
                return statusEnum;
        }
    }
}