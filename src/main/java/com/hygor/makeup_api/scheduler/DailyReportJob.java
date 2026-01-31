package com.hygor.makeup_api.scheduler;

import com.hygor.makeup_api.dto.admin.DashboardResponse;
import com.hygor.makeup_api.service.AdminDashboardService;
import com.hygor.makeup_api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class DailyReportJob {

    private final AdminDashboardService dashboardService;
    private final EmailService emailService;

    @Value("${app.mail.admin}")
    private String adminEmail;

    /**
     * Roda todos os dias à meia-noite (00:00:00)
     * Cron: Segundo Minuto Hora Dia Mês DiaSemana
     */
    @Scheduled(cron = "0 0 0 * * *") 
    public void sendDailyClosingReport() {
        log.info("Iniciando job de relatório diário...");

        // Pega os dados de ONTEM (Fechamento D-1)
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        DashboardResponse stats = dashboardService.getDashboardStats(yesterday, yesterday);

        // Só envia se teve vendas ou movimento relevante (opcional)
        emailService.sendDailyReport(adminEmail, stats, yesterday);
        
        log.info("Relatório diário enviado com sucesso.");
    }
}