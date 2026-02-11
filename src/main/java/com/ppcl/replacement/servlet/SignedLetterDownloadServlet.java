package com.ppcl.replacement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/signed-letter")
public class SignedLetterDownloadServlet extends HttpServlet {

    private static final String DEFAULT_SIGNED_OUTPUT_DIR = "/home/naruto/ppcl/signed_replacement_letter";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileName = req.getParameter("file");
        if (fileName == null || fileName.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file");
            return;
        }

        String outputDirParam = getServletContext().getInitParameter("dsc.signed.output.dir");
        String outputDir = outputDirParam != null ? outputDirParam : DEFAULT_SIGNED_OUTPUT_DIR;

        Path baseDir = Paths.get(outputDir).toAbsolutePath().normalize();
        Path requested = baseDir.resolve(fileName).normalize();
        if (!requested.startsWith(baseDir)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid file path");
            return;
        }

        if (!Files.exists(requested)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        String requestedName = requested.getFileName().toString();
        String requestedNameLower = requestedName.toLowerCase();
        if (requestedNameLower.endsWith(".zip")) {
            resp.setContentType("application/zip");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + requestedName + "\"");
        } else {
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + requestedName + "\"");
        }
        resp.setContentLengthLong(Files.size(requested));

        try (InputStream in = Files.newInputStream(requested);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
