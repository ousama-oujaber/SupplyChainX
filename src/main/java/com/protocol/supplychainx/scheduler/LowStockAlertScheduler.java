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
                .append("@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap');")
                .append("body { font-family: 'Roboto', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; background-color: #f4f4f4; margin: 0; padding: 20px; }")
                .append(".container { max-width: 800px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.15); }")
                .append(".header { background: linear-gradient(135deg, #d32f2f 0%, #f44336 100%); color: white; padding: 30px; text-align: center; position: relative; overflow: hidden; }")
                .append(".header::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: url('data:image/svg+xml,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 100 100\"><rect width=\"100\" height=\"100\" fill=\"none\"/><circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"none\" stroke=\"rgba(255,255,255,0.1)\" stroke-width=\"0.5\"/></svg>') repeat; opacity: 0.2; }")
                .append(".header h1 { margin: 0; font-size: 28px; font-weight: 600; position: relative; z-index: 1; }")
                .append(".header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; position: relative; z-index: 1; }")
                .append(".content { padding: 30px; }")
                .append(".alert-box { background-color: #fff3cd; border-left: 5px solid #ffc107; padding: 15px; margin-bottom: 25px; border-radius: 4px; display: flex; align-items: center; }")
                .append(".alert-icon { font-size: 24px; margin-right: 15px; }")
                .append(".alert-box p { margin: 0; color: #856404; font-size: 16px; font-weight: 500; }")
                .append("table { border-collapse: collapse; width: 100%; margin-top: 20px; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }")
                .append("th { background-color: #d32f2f; color: white; padding: 14px; text-align: left; font-weight: 600; font-size: 14px; text-transform: uppercase; letter-spacing: 0.5px; }")
                .append("td { border: 1px solid #e0e0e0; padding: 12px; font-size: 14px; }")
                .append("tr:nth-child(even) { background-color: #f9f9f9; }")
                .append("tr:hover { background-color: #f5f5f5; transition: background-color 0.3s ease; }")
                .append(".warning-value { color: #d32f2f; font-weight: bold; }")
                .append(".deficit { background-color: #ffebee; }")
                .append(".action-box { background-color: #e3f2fd; border-left: 5px solid #2196f3; padding: 15px; margin-top: 25px; border-radius: 4px; }")
                .append(".action-box strong { color: #1976d2; }")
                .append(".footer { background-color: #f5f5f5; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0; }")
                .append(".footer p { margin: 5px 0; color: #666; font-size: 12px; }")
                .append(".stats { display: flex; justify-content: space-around; margin: 20px 0; }")
                .append(".stat-item { text-align: center; padding: 15px; background-color: #f9f9f9; border-radius: 8px; flex: 1; margin: 0 10px; transition: transform 0.3s ease, box-shadow 0.3s ease; }")
                .append(".stat-item:hover { transform: translateY(-5px); box-shadow: 0 6px 12px rgba(0,0,0,0.1); }")
                .append(".stat-number { font-size: 32px; font-weight: bold; color: #d32f2f; margin: 0; }")
                .append(".stat-label { font-size: 14px; color: #666; margin: 5px 0 0 0; }")
                .append(".summary-box { background-color: #f8f9fa; border-radius: 8px; padding: 20px; margin-bottom: 25px; border: 1px solid #e9ecef; }")
                .append(".summary-title { font-size: 18px; font-weight: 500; color: #495057; margin-top: 0; margin-bottom: 15px; }")
                .append(".priority-high { color: #d32f2f; font-weight: bold; }")
                .append(".priority-medium { color: #ff9800; font-weight: bold; }")
                .append(".priority-low { color: #4caf50; font-weight: bold; }")
                .append(".chart-container { height: 200px; margin: 20px 0; }")
                .append("@media (max-width: 600px) { .stats { flex-direction: column; } .stat-item { margin: 10px 0; } }")
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
                .append("<p>Un total de <strong>").append(materials.size()).append("</strong> matières premières sont en stock critique avec un déficit total de <strong>").append(totalDeficit).append("</strong> unités.</p>")
                .append("<p>Priorités : <span class='priority-high'>Haute (").append(highPriorityCount).append(")</span> | ")
                .append("<span class='priority-medium'>Moyenne (").append(mediumPriorityCount).append(")</span> | ")
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
                    .append("<td>").append(material.getIdMaterial()).append("</td>")
                    .append("<td><strong>").append(material.getName()).append("</strong></td>")
                    .append("<td class='warning-value'>").append(material.getStock()).append("</td>")
                    .append("<td>").append(material.getStockMin()).append("</td>")
                    .append("<td>").append(material.getUnit()).append("</td>")
                    .append("<td class='warning-value deficit'>").append(deficit).append(" ").append(material.getUnit()).append("</td>")
                    .append("<td class='").append(priorityClass).append("'>").append(priorityText).append("</td>")
                    .append("</tr>");
        }

        html.append("</tbody>")
                .append("</table>")
                .append("<div class='action-box'>")
                .append("<p><strong>📋 Actions Requises :</strong></p>")
                .append("<ul style='margin: 10px 0; padding-left: 20px;'>")
                .append("<li>Créer des commandes d'approvisionnement pour ces matières</li>")
                .append("<li>Contacter les fournisseurs pour vérifier les délais de livraison</li>")
                .append("<li>Prioriser les commandes selon l'urgence de production</li>")
                .append("<li>Mettre à jour le système après création des commandes</li>")
                .append("<li>Considérer des alternatives temporaires si nécessaire</li>")
                .append("</ul>")
                .append("</div>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p><strong>SupplyChainX</strong> - Système de Gestion de la Supply Chain</p>")
                .append("<p>Cet email a été généré automatiquement par le système de surveillance des stocks.</p>")
                .append("<p>Pour toute question, contactez le département d'approvisionnement.</p>")
                .append("<p style='margin-top: 15px; color: #999;'>© 2025 SupplyChainX. Tous droits réservés.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }
}
