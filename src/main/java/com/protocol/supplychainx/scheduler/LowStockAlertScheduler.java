package com.protocol.supplychainx.scheduler;

import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.scheduler.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "scheduler.low-stock.enabled", havingValue = "true", matchIfMissing = true)
public class LowStockAlertScheduler {

    private final RawMaterialRepository rawMaterialRepository;
    private final EmailService emailService;

    @Value("${scheduler.low-stock.email-to:procurement@supplychainx.com}")
    private String emailTo;

    @Scheduled(cron = "${scheduler.low-stock.cron:0 0 9 * * ?}")
    public void checkLowStockMaterials() {
        log.info("==========================================");
        log.info("Starting low stock check at {}", LocalDateTime.now());
        log.info("==========================================");

        try {
            List<RawMaterial> lowStockMaterials = rawMaterialRepository.findByStockLessThanStockMin();

            if (lowStockMaterials.isEmpty()) {
                log.info("✓ No materials below minimum stock level");
                log.info("==========================================");
                return;
            }

            log.warn("⚠ Found {} material(s) below minimum stock level:", lowStockMaterials.size());
            lowStockMaterials.forEach(material ->
                    log.warn("  - {} (ID: {}): Stock={}, Min={}, Deficit={}",
                            material.getName(),
                            material.getIdMaterial(),
                            material.getStock(),
                            material.getStockMin(),
                            material.getStockMin() - material.getStock())
            );

            sendLowStockAlert(lowStockMaterials);

            log.info("✓ Low stock check completed successfully");
            log.info("==========================================");

        } catch (Exception e) {
            log.error("✗ Error during low stock check", e);
            log.info("==========================================");
        }
    }

    // @Scheduled(fixedRate = 3600000)
    // public void checkLowStockMaterialsHourly() {
    //     log.info("Running hourly low stock check");
    //     checkLowStockMaterials();
    // }

    // @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    // public void checkLowStockMaterialsWithDelay() {
    //     log.info("Running delayed low stock check");
    //     checkLowStockMaterials();
    // }

    private void sendLowStockAlert(List<RawMaterial> lowStockMaterials) {
        try {
            String subject = "⚠️ ALERTE STOCK CRITIQUE - SupplyChainX";
            String htmlBody = buildHtmlEmailBody(lowStockMaterials);

            if (emailTo.contains(",")) {
                String[] recipients = emailTo.split(",");
                emailService.sendHtmlEmailToMultiple(recipients, subject, htmlBody);
                log.info("✓ Low stock alert email sent to {} recipients", recipients.length);
            } else {
                emailService.sendHtmlEmail(emailTo, subject, htmlBody);
                log.info("✓ Low stock alert email sent to: {}", emailTo);
            }

        } catch (Exception e) {
            log.error("✗ Failed to send low stock alert email", e);
        }
    }

    private String buildHtmlEmailBody(List<RawMaterial> materials) {
        StringBuilder html = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        html.append("<!DOCTYPE html>")
                .append("<html lang='fr'>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<style>")
                .append("@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');")
                // Base styles - Dark mode theme matching SupplyChainX
                .append("body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #f8fafc; background-color: #020617; margin: 0; padding: 24px; -webkit-font-smoothing: antialiased; }")
                .append(".container { max-width: 800px; margin: 0 auto; background: rgba(15, 23, 42, 0.8); border-radius: 24px; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5); border: 1px solid rgba(255, 255, 255, 0.1); }")
                // Header with gradient - matching primary colors
                .append(".header { background: linear-gradient(135deg, #0ea5e9 0%, #6366f1 50%, #a78bfa 100%); padding: 40px; text-align: center; position: relative; overflow: hidden; }")
                .append(".header::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: url('data:image/svg+xml,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"60\" height=\"60\" viewBox=\"0 0 60 60\"><rect width=\"60\" height=\"60\" fill=\"none\"/><circle cx=\"30\" cy=\"30\" r=\"20\" fill=\"none\" stroke=\"rgba(255,255,255,0.1)\" stroke-width=\"0.5\"/></svg>') repeat; opacity: 0.3; }")
                .append(".header h1 { margin: 0; font-size: 28px; font-weight: 700; position: relative; z-index: 1; color: white; letter-spacing: -0.5px; }")
                .append(".header p { margin: 12px 0 0 0; font-size: 14px; opacity: 0.9; position: relative; z-index: 1; color: rgba(255,255,255,0.85); font-weight: 500; }")
                .append(".content { padding: 32px; background: rgba(15, 23, 42, 0.6); }")
                // Alert box with amber/warning styling
                .append(".alert-box { background: rgba(245, 158, 11, 0.1); border-left: 4px solid #fbbf24; padding: 16px 20px; margin-bottom: 28px; border-radius: 12px; display: flex; align-items: center; backdrop-filter: blur(8px); border: 1px solid rgba(251, 191, 36, 0.2); }")
                .append(".alert-icon { font-size: 24px; margin-right: 16px; }")
                .append(".alert-box p { margin: 0; color: #fbbf24; font-size: 15px; font-weight: 600; }")
                // Table styles - glassmorphism effect
                .append("table { border-collapse: separate; border-spacing: 0; width: 100%; margin-top: 24px; background: rgba(30, 41, 59, 0.4); border-radius: 16px; overflow: hidden; border: 1px solid rgba(255, 255, 255, 0.05); }")
                .append("th { background: rgba(15, 23, 42, 0.8); color: #94a3b8; padding: 16px; text-align: left; font-weight: 600; font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }")
                .append("td { border-bottom: 1px solid rgba(255, 255, 255, 0.03); padding: 14px 16px; font-size: 14px; color: #e2e8f0; }")
                .append("tr:last-child td { border-bottom: none; }")
                .append("tr:hover { background: rgba(255, 255, 255, 0.02); }")
                // Warning value styling - red accent
                .append(".warning-value { color: #f87171; font-weight: 600; }")
                .append(".deficit { background: rgba(239, 68, 68, 0.08); border-radius: 8px; padding: 4px 8px; }")
                // Action box - blue accent  
                .append(".action-box { background: rgba(59, 130, 246, 0.1); border-left: 4px solid #60a5fa; padding: 20px; margin-top: 28px; border-radius: 12px; border: 1px solid rgba(96, 165, 250, 0.2); }")
                .append(".action-box strong { color: #60a5fa; font-size: 15px; }")
                .append(".action-box ul { margin: 12px 0 0 0; padding-left: 20px; }")
                .append(".action-box li { color: #94a3b8; margin: 8px 0; font-size: 14px; }")
                // Footer - subtle dark styling
                .append(".footer { background: rgba(15, 23, 42, 0.9); padding: 24px 32px; text-align: center; border-top: 1px solid rgba(255, 255, 255, 0.05); }")
                .append(".footer p { margin: 6px 0; color: #64748b; font-size: 13px; }")
                .append(".footer .brand { font-weight: 700; background: linear-gradient(135deg, #60a5fa, #a78bfa); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; font-size: 15px; }")
                // Stats section matching dashboard KPI cards
                .append(".stats { display: flex; justify-content: space-around; margin: 24px 0; gap: 16px; }")
                .append(".stat-item { text-align: center; padding: 20px; background: rgba(30, 41, 59, 0.5); border-radius: 16px; flex: 1; border: 1px solid rgba(255, 255, 255, 0.05); transition: transform 0.3s ease; }")
                .append(".stat-item:hover { transform: translateY(-4px); }")
                .append(".stat-number { font-size: 32px; font-weight: 700; background: linear-gradient(135deg, #60a5fa, #a78bfa); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; margin: 0; }")
                .append(".stat-label { font-size: 12px; color: #64748b; margin: 8px 0 0 0; text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600; }")
                // Summary box styling
                .append(".summary-box { background: rgba(30, 41, 59, 0.4); border-radius: 16px; padding: 24px; margin-bottom: 28px; border: 1px solid rgba(255, 255, 255, 0.05); }")
                .append(".summary-title { font-size: 18px; font-weight: 600; color: #f8fafc; margin-top: 0; margin-bottom: 16px; display: flex; align-items: center; gap: 10px; }")
                // Priority badges matching status-badge styles
                .append(".priority-high { color: #f87171; font-weight: 600; background: rgba(239, 68, 68, 0.15); padding: 4px 12px; border-radius: 9999px; font-size: 12px; }")
                .append(".priority-medium { color: #fbbf24; font-weight: 600; background: rgba(245, 158, 11, 0.15); padding: 4px 12px; border-radius: 9999px; font-size: 12px; }")
                .append(".priority-low { color: #4ade80; font-weight: 600; background: rgba(34, 197, 94, 0.15); padding: 4px 12px; border-radius: 9999px; font-size: 12px; }")
                .append(".chart-container { height: 200px; margin: 20px 0; }")
                // Responsive table wrapper
                .append(".table-wrapper { overflow-x: auto; -webkit-overflow-scrolling: touch; margin: 24px -16px; padding: 0 16px; }")
                // Responsive design - comprehensive mobile styles
                .append("@media screen and (max-width: 600px) { ")
                .append("body { padding: 12px; } ")
                .append(".container { margin: 0; border-radius: 16px; } ")
                .append(".header { padding: 24px 16px; } ")
                .append(".header h1 { font-size: 22px; } ")
                .append(".header p { font-size: 12px; } ")
                .append(".content { padding: 16px; } ")
                .append(".stats { flex-direction: column; gap: 12px; } ")
                .append(".stat-item { margin: 0; padding: 16px; } ")
                .append(".stat-number { font-size: 26px; } ")
                .append(".stat-label { font-size: 11px; } ")
                .append(".summary-box { padding: 16px; } ")
                .append(".summary-title { font-size: 16px; } ")
                .append(".alert-box { flex-direction: column; text-align: center; padding: 16px; } ")
                .append(".alert-icon { margin: 0 0 8px 0; } ")
                .append(".alert-box p { font-size: 14px; } ")
                .append("table { font-size: 12px; display: block; overflow-x: auto; white-space: nowrap; } ")
                .append("th, td { padding: 10px 12px; } ")
                .append("th { font-size: 10px; } ")
                .append(".priority-high, .priority-medium, .priority-low { padding: 3px 8px; font-size: 10px; white-space: nowrap; } ")
                .append(".action-box { padding: 16px; } ")
                .append(".action-box strong { font-size: 14px; } ")
                .append(".action-box li { font-size: 13px; } ")
                .append(".footer { padding: 20px 16px; } ")
                .append(".footer p { font-size: 11px; } ")
                .append("}")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h1>⚠️ Alerte Stock Critique</h1>")
                .append("<p>SupplyChainX - Système de Gestion de la Supply Chain</p>")
                .append("<p>").append(LocalDateTime.now().format(formatter)).append("</p>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<div class='alert-box'>")
                .append("<div class='alert-icon'>⚠️</div>")
                .append("<p>Attention : Des matières premières nécessitent un réapprovisionnement immédiat !</p>")
                .append("</div>");

        int totalDeficit = materials.stream()
                .mapToInt(m -> m.getStockMin() - m.getStock())
                .sum();

        long highPriorityCount = materials.stream()
                .filter(m -> (m.getStockMin() - m.getStock()) > 50)
                .count();

        long mediumPriorityCount = materials.stream()
                .filter(m -> (m.getStockMin() - m.getStock()) > 20 && (m.getStockMin() - m.getStock()) <= 50)
                .count();

        long lowPriorityCount = materials.stream()
                .filter(m -> (m.getStockMin() - m.getStock()) <= 20)
                .count();

        html.append("<div class='summary-box'>")
                .append("<h3 class='summary-title'>📊 Résumé de l'Alerte</h3>")
                .append("<p style='color: #94a3b8; margin: 0 0 12px 0; font-size: 14px; line-height: 1.6;'>Un total de <strong style='color: #f8fafc;'>").append(materials.size()).append("</strong> matières premières sont en stock critique avec un déficit total de <strong style='color: #f8fafc;'>").append(totalDeficit).append("</strong> unités.</p>")
                .append("<p style='color: #94a3b8; margin: 0; font-size: 14px;'>Priorités : <span class='priority-high'>Haute (").append(highPriorityCount).append(")</span> ")
                .append("<span class='priority-medium'>Moyenne (").append(mediumPriorityCount).append(")</span> ")
                .append("<span class='priority-low'>Basse (").append(lowPriorityCount).append(")</span></p>")
                .append("</div>");

        html.append("<div class='stats'>")
                .append("<div class='stat-item'>")
                .append("<p class='stat-number'>").append(materials.size()).append("</p>")
                .append("<p class='stat-label'>Matières en Alerte</p>")
                .append("</div>")
                .append("<div class='stat-item'>")
                .append("<p class='stat-number'>").append(totalDeficit).append("</p>")
                .append("<p class='stat-label'>Déficit Total</p>")
                .append("</div>")
                .append("<div class='stat-item'>")
                .append("<p class='stat-number'>").append(highPriorityCount).append("</p>")
                .append("<p class='stat-label'>Haute Priorité</p>")
                .append("</div>")
                .append("</div>");

        html.append("<table>")
                .append("<thead>")
                .append("<tr>")
                .append("<th>ID</th>")
                .append("<th>Nom de la Matière</th>")
                .append("<th>Stock Actuel</th>")
                .append("<th>Stock Minimum</th>")
                .append("<th>Unité</th>")
                .append("<th>Déficit</th>")
                .append("<th>Priorité</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");

        for (RawMaterial material : materials) {
            int deficit = material.getStockMin() - material.getStock();
            String priorityClass = "";
            String priorityText = "";

            if (deficit > 50) {
                priorityClass = "priority-high";
                priorityText = "Haute";
            } else if (deficit > 20) {
                priorityClass = "priority-medium";
                priorityText = "Moyenne";
            } else {
                priorityClass = "priority-low";
                priorityText = "Basse";
            }

            html.append("<tr>")
                    .append("<td style='color: #64748b; font-weight: 500;'>").append(material.getIdMaterial()).append("</td>")
                    .append("<td><strong style='color: #f8fafc;'>").append(material.getName()).append("</strong></td>")
                    .append("<td class='warning-value'>").append(material.getStock()).append("</td>")
                    .append("<td style='color: #94a3b8;'>").append(material.getStockMin()).append("</td>")
                    .append("<td style='color: #64748b;'>").append(material.getUnit()).append("</td>")
                    .append("<td><span class='deficit warning-value'>").append(deficit).append(" ").append(material.getUnit()).append("</span></td>")
                    .append("<td><span class='").append(priorityClass).append("'>").append(priorityText).append("</span></td>")
                    .append("</tr>");
        }

        html.append("</tbody>")
                .append("</table>")
                .append("<div class='action-box'>")
                .append("<p><strong>📋 Actions Requises :</strong></p>")
                .append("<ul>")
                .append("<li>Créer des commandes d'approvisionnement pour ces matières</li>")
                .append("<li>Contacter les fournisseurs pour vérifier les délais de livraison</li>")
                .append("<li>Prioriser les commandes selon l'urgence de production</li>")
                .append("<li>Mettre à jour le système après création des commandes</li>")
                .append("<li>Considérer des alternatives temporaires si nécessaire</li>")
                .append("</ul>")
                .append("</div>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p class='brand'>SupplyChainX</p>")
                .append("<p>Système de Gestion de la Supply Chain</p>")
                .append("<p>Cet email a été généré automatiquement par le système de surveillance des stocks.</p>")
                .append("<p>Pour toute question, contactez le département d'approvisionnement.</p>")
                .append("<p style='margin-top: 16px; color: #475569;'>© 2025 SupplyChainX. Tous droits réservés.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }
}
