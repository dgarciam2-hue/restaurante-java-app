package com.devtalles.proyecto.utils;

import com.devtalles.proyecto.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {
    private static final String CARPETA = "facturas";
    private static final int ANCHO = 64;
    private static final int LINEAS_POR_PAGINA = 52;
    private static final Locale LOCALE = new Locale("es", "CO");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera la factura en PDF, la guarda en la carpeta "facturas/" y retorna la ruta del archivo generado.
     */
    public String generar(Invoice factura) throws IOException {
        if (factura == null || factura.getOrder() == null) {
            throw new IllegalArgumentException("La factura o el pedido asociado no pueden ser nulos.");
        }

        // 1. Asegurar que exista la carpeta de destino
        Path carpetaPath = Paths.get(CARPETA);
        if (!Files.exists(carpetaPath)) {
            Files.createDirectories(carpetaPath);
        }

        // 2. Dar formato de cuadrícula textual a la factura
        List<String> lineas = armarLineas(factura);

        // 3. Convertir las líneas de texto en el flujo de bytes de un archivo PDF válido
        byte[] pdfBytes = construirPdf(lineas);

        // 4. Guardar archivo PDF en disco
        String nombreArchivo = String.format("%s/factura_%d.pdf", CARPETA, factura.getOrder().getOrderId());
        Path archivoPath = Paths.get(nombreArchivo);
        Files.write(archivoPath, pdfBytes);

        return archivoPath.toAbsolutePath().toString();
    }

    /**
     * Formatea los datos de la factura en filas cuadradas de exactamente 64 caracteres de ancho.
     */
    private List<String> armarLineas(Invoice factura) {
        List<String> lineas = new ArrayList<>();
        String sep = "-".repeat(ANCHO);
        String sepDoble = "=".repeat(ANCHO);

        // --- ENCABEZADO Y RESTAURANTE ---
        lineas.add(center("RESTAURANTE DEV-TALLES", ANCHO));
        lineas.add(center("NIT: 900.123.456-1", ANCHO));
        lineas.add(center("Cartagena, Colombia", ANCHO));
        lineas.add(sepDoble);

        lineas.add(formatTwoColumns("FACTURA DE VENTA N°:", String.valueOf(factura.getOrder().getOrderId()), ANCHO));
        lineas.add(formatTwoColumns("Fecha de Emisión:", factura.getIssueDate().format(FORMATO_FECHA), ANCHO));

        // --- DATOS DE DESTINO ---
        if (factura instanceof InvoiceTable invoiceTable) {
            lineas.add(formatTwoColumns("Tipo de Servicio:", "Consumo en Mesa", ANCHO));
            lineas.add(formatTwoColumns("Mesa N°:", invoiceTable.getDestinationData().get(0), ANCHO));
            lineas.add(formatTwoColumns("Selección Propina:", invoiceTable.getTipType().toString(), ANCHO));
        } else if (factura instanceof InvoiceDelivery invoiceDelivery) {
            lineas.add(formatTwoColumns("Tipo de Servicio:", "Domicilio", ANCHO));
            List<String> datos = invoiceDelivery.getDestinationData();
            if (datos.size() >= 4) {
                lineas.add(formatTwoColumns("Cliente:", datos.get(0), ANCHO));
                lineas.add(formatTwoColumns("Teléfono:", datos.get(1), ANCHO));
                lineas.add(formatTwoColumns("Dirección:", datos.get(2), ANCHO));
                lineas.add(formatTwoColumns("Zona Entrega:", datos.get(3), ANCHO));
            }
        }

        lineas.add(sep);

        // --- ENCABEZADO DE TABLA DE PRODUCTOS ---
        // Distribución 64 cols: Producto (28) | Cant (6) | Precio U. (14) | Total (16)
        lineas.add(padRight("PRODUCTO", 28) + padLeft("CANT", 6) + padLeft("PRECIO U.", 14) + padLeft("TOTAL", 16));
        lineas.add(sep);

        // --- DETALLE DE PRODUCTOS ---
        if (factura.getOrder().getDetails() != null) {
            for (OrderDetail detail : factura.getOrder().getDetails()) {
                String name = detail.getProduct().getNameProduct();
                if (name.length() > 27) {
                    name = name.substring(0, 24) + "...";
                }

                String qty = String.valueOf(detail.getAmount());
                String price = String.format(LOCALE, "$%,.0f", detail.getProduct().getPriceProduct());
                double itemTotalVal = detail.getProduct().getPriceProduct() * detail.getAmount();
                String itemTotal = String.format(LOCALE, "$%,.0f", itemTotalVal);

                lineas.add(padRight(name, 28) + padLeft(qty, 6) + padLeft(price, 14) + padLeft(itemTotal, 16));
            }
        }

        lineas.add(sep);

        // --- CÁLCULOS Y LIQUIDACIÓN FINANCIERA ---
        double subtotal = factura.getSubtotal();
        double discount = factura.getBestDiscount();
        double netBase = Math.max(0, subtotal - discount);
        double tax = netBase * Invoice.TAX_RATE;

        double additionalValue = 0.0;
        String labelAdicional = "Adicional:";

        if (factura instanceof InvoiceTable invoiceTable) {
            additionalValue = invoiceTable.getTipAmount();
            labelAdicional = "Propina (" + invoiceTable.getTipType() + "):";
        } else if (factura instanceof InvoiceDelivery) {
            additionalValue = factura.getOrder().calculateAdditionalValue();
            labelAdicional = "Recargo Domicilio:";
        }

        double totalFinal = netBase + tax + additionalValue;

        lineas.add(formatTwoColumns("Subtotal Bruto:", String.format(LOCALE, "$%,.2f", subtotal), ANCHO));
        if (discount > 0) {
            lineas.add(formatTwoColumns("Descuento Aplicado:", String.format(LOCALE, "-$%,.2f", discount), ANCHO));
        }
        lineas.add(formatTwoColumns("Base Neta Gravable:", String.format(LOCALE, "$%,.2f", netBase), ANCHO));
        lineas.add(formatTwoColumns("Impoconsumo (INC 8%):", String.format(LOCALE, "$%,.2f", tax), ANCHO));
        lineas.add(formatTwoColumns(labelAdicional, String.format(LOCALE, "$%,.2f", additionalValue), ANCHO));

        lineas.add(sepDoble);
        lineas.add(formatTwoColumns("TOTAL A PAGAR:", String.format(LOCALE, "$%,.2f", totalFinal), ANCHO));
        lineas.add(sepDoble);

        lineas.add("");
        lineas.add(center("¡Gracias por su compra!", ANCHO));
        lineas.add(center("Conserve este comprobante de pago.", ANCHO));

        return lineas;
    }

    /**
     * Construye la estructura interna de un documento PDF binario nativo (PDF-1.4).
     */
    private byte[] construirPdf(List<String> lineas) throws IOException {
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i += LINEAS_POR_PAGINA) {
            paginas.add(lineas.subList(i, Math.min(i + LINEAS_POR_PAGINA, lineas.size())));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Long> offsets = new ArrayList<>();
        offsets.add(0L);

        out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

        // Objeto 1: Catalog
        offsets.add((long) out.size());
        out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

        // Objeto 2: Pages
        int totalPaginas = paginas.size();
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < totalPaginas; i++) {
            kids.append((3 + i * 2)).append(" 0 R ");
        }
        offsets.add((long) out.size());
        out.write(String.format("2 0 obj\n<< /Type /Pages /Kids [%s] /Count %d >>\nendobj\n",
                kids.toString().trim(), totalPaginas).getBytes(StandardCharsets.US_ASCII));

        int fontObjId = 3 + totalPaginas * 2;

        // Páginas y Contenidos
        for (int i = 0; i < totalPaginas; i++) {
            int pageObjId = 3 + i * 2;
            int contentObjId = pageObjId + 1;

            offsets.add((long) out.size());
            out.write(String.format("%d 0 obj\n<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 %d 0 R >> >> /MediaBox [0 0 612 792] /Contents %d 0 R >>\nendobj\n",
                    pageObjId, fontObjId, contentObjId).getBytes(StandardCharsets.US_ASCII));

            byte[] contentBytes = contenidoPagina(paginas.get(i));
            offsets.add((long) out.size());
            out.write(String.format("%d 0 obj\n<< /Length %d >>\nstream\n", contentObjId, contentBytes.length).getBytes(StandardCharsets.US_ASCII));
            out.write(contentBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        }

        // Objeto Fuente: Courier Monoespaciada
        offsets.add((long) out.size());
        out.write(String.format("%d 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n", fontObjId).getBytes(StandardCharsets.US_ASCII));

        // Tabla Cross-Reference (XRef)
        long xrefOffset = out.size();
        out.write("xref\n".getBytes(StandardCharsets.US_ASCII));
        out.write(String.format("0 %d\n", offsets.size()).getBytes(StandardCharsets.US_ASCII));
        out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (int i = 1; i < offsets.size(); i++) {
            out.write(String.format(Locale.US, "%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.US_ASCII));
        }

        // Trailer del PDF
        out.write(String.format("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%EOF", offsets.size(), xrefOffset).getBytes(StandardCharsets.US_ASCII));

        return out.toByteArray();
    }

    /**
     * Dibuja las líneas en el lienzo de la página actual utilizando comandos PDF.
     */
    private byte[] contenidoPagina(List<String> pagina) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n");
        sb.append("/F1 10 Tf\n"); // Fuente Courier a 10pt
        sb.append("12 TL\n");    // Interlineado de 12pt
        sb.append("40 750 Td\n"); // Posición inicial en (X:40, Y:750)

        for (String linea : pagina) {
            String escaped = linea.replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)");
            sb.append("(").append(escaped).append(") '\n");
        }

        sb.append("ET\n");
        return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    // --- MÉTODOS AUXILIARES DE FORMATO TEXTUAL ---

    private String padRight(String s, int n) {
        if (s.length() >= n) return s.substring(0, n);
        return String.format("%-" + n + "s", s);
    }

    private String padLeft(String s, int n) {
        if (s.length() >= n) return s.substring(0, n);
        return String.format("%" + n + "s", s);
    }

    private String center(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        int leftPadding = (width - s.length()) / 2;
        int rightPadding = width - s.length() - leftPadding;
        return " ".repeat(leftPadding) + s + " ".repeat(rightPadding);
    }

    private String formatTwoColumns(String left, String right, int width) {
        int space = width - left.length() - right.length();
        if (space < 1) {
            return padRight(left, width - right.length() - 1) + " " + right;
        }
        return left + " ".repeat(space) + right;
    }
}
