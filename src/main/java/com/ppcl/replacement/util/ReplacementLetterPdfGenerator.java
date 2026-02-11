package com.ppcl.replacement.util;

import com.itextpdf.html2pdf.HtmlConverter;
import com.ppcl.replacement.model.CartridgeDetail;
import com.ppcl.replacement.model.ReplacementLetterData;
import com.ppcl.replacement.model.ReplacementPrinter;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Generates a PDF from ReplacementLetterData using iText html2pdf.
 */
public class ReplacementLetterPdfGenerator {

    public static byte[] generate(final ReplacementLetterData data) throws Exception {
        final String html = buildHtml(data);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, bos);
        return bos.toByteArray();
    }

    private static String buildHtml(final ReplacementLetterData data) {
        final StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; font-size: 12px; color: #333; padding: 40px; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 15px; }");
        sb.append("th, td { border: 1px solid #999; padding: 6px 8px; text-align: left; font-size: 11px; }");
        sb.append("th { background-color: #f0f0f0; }");
        sb.append(".header-row { margin-bottom: 30px; }");
        sb.append(".signature-box { height: 80px; border: 1px solid #ccc; text-align: center; ");
        sb.append("  display: flex; align-items: center; justify-content: center; margin-bottom: 8px; }");
        sb.append(".signed { color: #0056b3; font-weight: bold; }");
        sb.append(".sig-marker { font-size: 1px; color: #fff; }");
        sb.append("</style></head><body>");

        // Letter Header
        sb.append("<table style='border:none; margin-bottom: 30px;'>");
        sb.append("<tr style='border:none;'>");
        sb.append("<td style='border:none; width:50%; vertical-align:top;'>");
        sb.append("<strong>To,</strong><br/>");
        sb.append(esc(data.getClient().getName())).append("<br/>");
        sb.append(esc(nvl(data.getClient().getCity()))).append(", ").append(esc(nvl(data.getClient().getState())));
        sb.append("</td>");
        sb.append("<td style='border:none; width:50%; text-align:right; vertical-align:top;'>");
        sb.append("<strong>REF NO: ").append(esc(data.getRefNo())).append("</strong><br/>");
        sb.append("DATE: ").append(esc(data.getLetterDate()));
        sb.append("</td></tr></table>");

        sb.append("<p><strong>CLIENT NAME:</strong> ").append(esc(data.getClient().getName())).append("</p>");
        sb.append("<p><strong>Subject:</strong> Replacement of Printer</p>");
        sb.append("<p>Dear Sir/Ma'am,</p>");
        sb.append("<p>This is to bring to your notice that, we are replacing the printer with another model for better and efficient service.</p>");
        sb.append("<p>Kindly find the printer details mentioned below.</p>");

        // Printer Details Table
        final List<ReplacementPrinter> printers = data.getPrinters();
        if (printers != null && !printers.isEmpty()) {
            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<th>Sr. No.</th>");
            sb.append("<th>Agreement Details</th>");
            sb.append("<th>Old Printer Model</th>");
            sb.append("<th>Old Serial Number</th>");
            sb.append("<th>New Printer Model</th>");
            sb.append("<th>New Serial Number</th>");
            sb.append("<th>Status</th>");
            sb.append("</tr>");

            for (int i = 0; i < printers.size(); i++) {
                final ReplacementPrinter p = printers.get(i);
                sb.append("<tr>");
                sb.append("<td style='text-align:center;'>").append(i + 1).append("</td>");
                sb.append("<td>").append(esc(nvl(p.getAgreementNoMapped())));
                if (p.getAgreementDate() != null) {
                    sb.append("<br/><small>").append(esc(p.getAgreementDate())).append("</small>");
                }
                sb.append("</td>");
                sb.append("<td>").append(esc(nvl(p.getExistingModelName()))).append("</td>");
                sb.append("<td>").append(esc(nvl(p.getExistingSerial()))).append("</td>");
                sb.append("<td>").append(esc(nvl(p.getNewModelName()))).append("</td>");
                sb.append("<td>").append(nvl(p.getNewSerial(), "-")).append("</td>");
                sb.append("<td style='text-align:center;'>").append(esc(nvl(p.getPrinterStage(), "Pending"))).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        // Cartridge Details
        final List<CartridgeDetail> cartridges = data.getCartridges();
        if (cartridges != null && !cartridges.isEmpty()) {
            sb.append("<p style='margin-top:30px;'><strong>Cartridges to be pulled back:</strong></p>");
            sb.append("<table>");
            sb.append("<tr><th>Sr. No.</th><th>Type</th><th>Cartridge Model</th><th>Quantity</th><th>Modification (if any)</th></tr>");

            for (int i = 0; i < cartridges.size(); i++) {
                final CartridgeDetail c = cartridges.get(i);
                sb.append("<tr>");
                sb.append("<td style='text-align:center;'>").append(i + 1).append("</td>");
                sb.append("<td>").append(esc(nvl(c.getType()))).append("</td>");
                sb.append("<td>").append(esc(nvl(c.getModel()))).append("</td>");
                sb.append("<td>").append(c.getQuantity()).append("</td>");
                sb.append("<td></td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        sb.append("<p style='margin-top:30px;'>Kindly acknowledge the receipt.</p>");

        // Signatures
        sb.append("<table style='border:none; margin-top:40px;'>");
        sb.append("<tr style='border:none;'>");
        sb.append("<td style='border:none; width:50%; text-align:center;'>");
        if (data.isSigned()) {
            sb.append("<div class='signature-box'>");
            sb.append("<span class='sig-marker'>PPCL_SIG_MARKER</span>");
            sb.append("</div>");
        } else {
            sb.append("<div class='signature-box'>Digital Signature Required</div>");
        }
        sb.append("<p><strong>Your's Truly,</strong><br/>PowerPoint Cartridges Pvt Ltd</p>");
        sb.append("</td>");
        sb.append("<td style='border:none; width:50%; text-align:center;'>");
        sb.append("<div class='signature-box'>Client Signature</div>");
        sb.append("<p><strong>Your's Truly,</strong><br/>").append(esc(data.getClient().getName())).append("</p>");
        sb.append("</td>");
        sb.append("</tr></table>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private static String esc(final String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String nvl(final String s) {
        return s != null ? s : "";
    }

    private static String nvl(final String s, final String def) {
        return (s != null && !s.isEmpty()) ? s : def;
    }
}
