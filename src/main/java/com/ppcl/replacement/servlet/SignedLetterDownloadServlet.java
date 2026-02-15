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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileName = req.getParameter("file");
        if (fileName == null || fileName.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file");
            return;
        }

        Path baseDir = resolveConfiguredPath("dsc.signed.output.dir");
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

    private Path resolveConfiguredPath(final String initParamName) {
        final String configuredValue = getServletContext().getInitParameter(initParamName);
        final Path appRootPath = getAppRootPath();

        if (configuredValue == null || configuredValue.trim().isEmpty()) {
            throw new IllegalStateException("Missing required servlet init-param: " + initParamName);
        }

        Path path = Paths.get(configuredValue.trim());
        if (!path.isAbsolute()) {
            path = appRootPath.resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }

    private Path getAppRootPath() {
        final String appRoot = getServletContext().getRealPath("");
        if (appRoot == null || appRoot.trim().isEmpty()) {
            return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        }
        return Paths.get(appRoot).toAbsolutePath().normalize();
    }
}
