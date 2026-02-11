package com.ppcl.replacement.util;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonResponse {

    public static void sendSuccess(final HttpServletResponse response, final String message, final Object data) throws IOException {
        final Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);
        sendJson(response, result);
    }

    public static void sendError(final HttpServletResponse response, final String message) throws IOException {
        final Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        sendJson(response, result);
    }

    private static void sendJson(final HttpServletResponse response, final Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(data));
        out.flush();
    }
}
