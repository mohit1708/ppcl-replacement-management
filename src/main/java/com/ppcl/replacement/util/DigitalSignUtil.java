package com.ppcl.replacement.util;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.signatures.*;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Set;

/**
 * PDF signer using iText 7 and JDK-only crypto.
 * Works with .p12/.pfx DSC.
 */
public class DigitalSignUtil {
    private static final String SIGNATURE_MARKER = "PPCL_SIG_MARKER";
    private static final float SIGNATURE_WIDTH = 180f;
    private static final float SIGNATURE_HEIGHT = 60f;

    /**
     * Signs a PDF using DSC (stream-based).
     *
     * @param srcPdf      Source PDF input stream
     * @param destPdf     Output signed PDF output stream
     * @param p12Path     Path to .p12/.pfx file
     * @param p12Password Password for DSC
     * @param reason      Signature reason
     * @param location    Signature location
     * @param visible     True if visible signature
     */
    public static void signPdf(
            InputStream srcPdf,
            OutputStream destPdf,
            String p12Path,
            char[] p12Password,
            String reason,
            String location,
            boolean visible
    ) throws Exception {
        byte[] srcBytes = toBytes(srcPdf);
        signPdf(srcBytes, destPdf, p12Path, p12Password, reason, location, visible);
    }

    private static void signPdf(
            byte[] srcPdfBytes,
            OutputStream destPdf,
            String p12Path,
            char[] p12Password,
            String reason,
            String location,
            boolean visible
    ) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream fis = new FileInputStream(p12Path)) {
            ks.load(fis, p12Password);
        }

        String alias = ks.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, p12Password);
        Certificate[] chain = ks.getCertificateChain(alias);

        PdfSigner signer = new PdfSigner(
                new PdfReader(new ByteArrayInputStream(srcPdfBytes)),
                destPdf,
                new StampingProperties()
        );

        PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason(reason)
                .setLocation(location)
                .setReuseAppearance(false);

        if (visible) {
            SignaturePosition position = findSignaturePosition(srcPdfBytes);
            if (position != null) {
                appearance
                        .setPageRect(position.rect)
                        .setPageNumber(position.pageNumber);
            } else {
            appearance
                        .setPageRect(new Rectangle(55, 105, 180, 60))
                        .setPageNumber(1);
            }
        }

        signer.setFieldName("Signature1");

        IExternalSignature signature = new PrivateKeySignature(
                privateKey,
                DigestAlgorithms.SHA256,
                null
        );

        IExternalDigest digest = new DigitalSignUtil.DefaultDigest();

        signer.signDetached(
                digest,
                signature,
                chain,
                null,
                null,
                null,
                0,
                PdfSigner.CryptoStandard.CADES
        );
    }

    /**
     * Signs a PDF using DSC (file-path-based convenience method).
     */
    public static void signPdf(
            String srcPdf,
            String destPdf,
            String p12Path,
            char[] p12Password,
            String reason,
            String location,
            boolean visible
    ) throws Exception {
        try (InputStream in = new FileInputStream(srcPdf);
             OutputStream out = new FileOutputStream(destPdf)) {
            signPdf(in, out, p12Path, p12Password, reason, location, visible);
        }
    }

    /**
     * Signs a PDF byte array and returns the signed PDF bytes.
     */
    public static byte[] signPdf(
            byte[] pdfBytes,
            String p12Path,
            char[] p12Password,
            String reason,
            String location,
            boolean visible
    ) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        signPdf(pdfBytes, bos, p12Path, p12Password, reason, location, visible);
        return bos.toByteArray();
    }

    /**
     * Default JDK digest implementation for file-based .p12/.pfx DSC.
     */
    static class DefaultDigest implements IExternalDigest {
        @Override
        public MessageDigest getMessageDigest(String hashAlgorithm)
                throws NoSuchAlgorithmException, NoSuchProviderException {
            return MessageDigest.getInstance(hashAlgorithm);
        }
    }

    private static SignaturePosition findSignaturePosition(byte[] pdfBytes) throws IOException {
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            int pages = pdfDoc.getNumberOfPages();
            for (int page = 1; page <= pages; page++) {
                MarkerListener listener = new MarkerListener();
                PdfCanvasProcessor processor = new PdfCanvasProcessor(listener);
                processor.processPageContent(pdfDoc.getPage(page));
                Rectangle markerRect = listener.getMarkerRect();
                if (markerRect != null) {
                    float centerX = (markerRect.getLeft() + markerRect.getRight()) / 2f;
                    float centerY = (markerRect.getBottom() + markerRect.getTop()) / 2f;
                    Rectangle sigRect = new Rectangle(
                            centerX - (SIGNATURE_WIDTH / 2f),
                            centerY - (SIGNATURE_HEIGHT / 2f),
                            SIGNATURE_WIDTH,
                            SIGNATURE_HEIGHT
                    );
                    return new SignaturePosition(sigRect, page);
                }
            }
        }
        return null;
    }

    private static byte[] toBytes(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }

    private static class MarkerListener implements IEventListener {
        private Rectangle markerRect;

        @Override
        public void eventOccurred(IEventData data, EventType type) {
            if (type != EventType.RENDER_TEXT || markerRect != null) {
                return;
            }
            TextRenderInfo info = (TextRenderInfo) data;
            String text = info.getText();
            if (text != null && text.contains(SIGNATURE_MARKER)) {
                Rectangle rect = info.getAscentLine().getBoundingRectangle();
                if (rect == null) {
                    rect = info.getBaseline().getBoundingRectangle();
                }
                markerRect = rect;
            }
        }

        @Override
        public Set<EventType> getSupportedEvents() {
            return Collections.singleton(EventType.RENDER_TEXT);
        }

        Rectangle getMarkerRect() {
            return markerRect;
        }
    }

    private static class SignaturePosition {
        private final Rectangle rect;
        private final int pageNumber;

        private SignaturePosition(Rectangle rect, int pageNumber) {
            this.rect = rect;
            this.pageNumber = pageNumber;
        }
    }
}
