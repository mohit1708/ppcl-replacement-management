package com.ppcl.replacement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Random;
import java.util.stream.Collectors;

public class WhatsAppMessageSender {

	public static String sendWhatsAppMessage(String phoneNumber, String templateId, String vendorId, String amount, String designation, HttpServletRequest request, HttpServletResponse response) {
		String message = "";
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = DBConnectionPool.getConnection();
	        String otp = generateOTP(4);

	        // Step 2: Calculate expiry time (5 minutes from now)
	        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes in milliseconds


	        // Step 3: Store OTP and expiry time in session
	        HttpSession session = request.getSession();
	        session.setAttribute("otp", otp);
	        session.setAttribute("otpExpiryTime", expiryTime);

			// WhatsApp API URL
			String phoneNumberId = "918879112211";
			String accessToken = "b42b413d-5017-11ef-ad4f-92672d2d0c2d";
			String apiUrl = "https://partners.pinbot.ai/v2/messages";
			String link="";
/*
			if(request.getScheme().equalsIgnoreCase("https"))
			{
				link = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/ERP_VENDOR/VendorLoginOtp.do?vendorId=" + EncryptionUtil.encrypt(vendorId);
			}
			else
			{
				link = "https://crm.powerpointcart.com/ERP_VENDOR/VendorLoginOtp.do?vendorId=" + EncryptionUtil.encrypt(vendorId);
			}
*/
			link = "https://crm.powerpointcart.com/ERP_VENDOR/VendorLoginOtp.do?vendorId=" + EncryptionUtil.encrypt(vendorId);

			String templateName = templateId;
			String payload = "{"
							+ "\"messaging_product\": \"whatsapp\","
							+ "\"recipient_type\": \"individual\","
							+ "\"to\": \"" + phoneNumber + "\","
							+ "\"type\": \"template\","
							+ "\"template\": {"
							+ "	\"language\": {"
							+ "\"code\": \"en\""
							+ "},"
							+ "\"name\": \"" + templateName + "\","
							+ "\"components\": ["
							+ "{"
							+ "\"type\": \"body\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + otp + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + link + "\""
							+ "}"
							+ "]"
							+ "}"
							+ "]"
							+ "}"
							+ "}";

			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("apikey", accessToken);
			conn.setRequestProperty("wanumber", phoneNumberId);
			conn.setDoOutput(true);

			// Send the payload
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = conn.getResponseCode();

			try (InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream()
					: conn.getErrorStream()) {
				String responseJson = new BufferedReader(new InputStreamReader(responseStream)).lines()
						.collect(Collectors.joining("\n"));
			}

			// Check the response
			if (status == HttpURLConnection.HTTP_OK)
			{
				message = "Message is submitted!";
				String updateVendorQuery = "UPDATE VEN SET OTP=?, OTP_GENERATE_TIME=? WHERE ID=?";
				preparedStatement = connection.prepareStatement(updateVendorQuery);
				preparedStatement.setString(1, otp);
				preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				preparedStatement.setString(3, vendorId);
				long rs2 = preparedStatement.executeUpdate();
			}
			else
			{
				message = "Failed to submit the message. HTTP Status Code: " + status;
			}

			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return message;
	}

	public static void sendRejectMessage(String phoneNumber, String templateId, String vendorId, String amount, String designation, String callId, String callDate, String clientName, String comment, String coordinatorEmail, String coordinatorNumber,  HttpServletRequest request, HttpServletResponse response) {
		try {

			Connection connection = DBConnectionPool.getConnection();
	        String otp = generateOTP(4);

	        // Step 2: Calculate expiry time (5 minutes from now)
	        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes in milliseconds


	        // Step 3: Store OTP and expiry time in session
	        HttpSession session = request.getSession();
	        session.setAttribute("otp", otp);
	        session.setAttribute("otpExpiryTime", expiryTime);

			// WhatsApp API URL
			String phoneNumberId = "918879112211";
			String accessToken = "b42b413d-5017-11ef-ad4f-92672d2d0c2d";
			String apiUrl = "https://partners.pinbot.ai/v2/messages";
			String link = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/ERP_VENDOR/VendorLoginOtp.do?vendorId=" + EncryptionUtil.encrypt(vendorId);
			String templateName = templateId;
			String payload = "{"
							+ "\"messaging_product\": \"whatsapp\","
							+ "\"recipient_type\": \"individual\","
							+ "\"to\": \"" + phoneNumber + "\","
							+ "\"type\": \"template\","
							+ "\"template\": {"
							+ "	\"language\": {"
							+ "\"code\": \"en\""
							+ "},"
							+ "\"name\": \"" + templateName + "\","
							+ "\"components\": ["
							+ "{"
							+ "\"type\": \"body\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + callId + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + callDate + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + clientName + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + (StringUtil.isNotBlank(comment) ? comment : "N/A") + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + coordinatorNumber + "/" + coordinatorEmail + "\""
							+ "}"
							+ "]"
							+ "}"
							+ "]"
							+ "}"
							+ "}";

			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("apikey", accessToken);
			conn.setRequestProperty("wanumber", phoneNumberId);
			conn.setDoOutput(true);

			// Send the payload
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = conn.getResponseCode();

			try (InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream()
					: conn.getErrorStream()) {
				String responseJson = new BufferedReader(new InputStreamReader(responseStream)).lines()
						.collect(Collectors.joining("\n"));
			}

			conn.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendPaymentSuccessfulMessage(String phoneNumber, String templateId, String vendorId, String amount, String callId, String callDate, String clientName, String comment, String coordinatorEmail, String coordinatorNumber,  HttpServletRequest request, HttpServletResponse response) {
		try {

			Connection connection = DBConnectionPool.getConnection();
	        String otp = generateOTP(4);

	        // Step 2: Calculate expiry time (5 minutes from now)
	        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes in milliseconds


	        // Step 3: Store OTP and expiry time in session
	        HttpSession session = request.getSession();
	        session.setAttribute("otp", otp);
	        session.setAttribute("otpExpiryTime", expiryTime);

			// WhatsApp API URL
			String phoneNumberId = "918879112211";
			String accessToken = "b42b413d-5017-11ef-ad4f-92672d2d0c2d";
			String apiUrl = "https://partners.pinbot.ai/v2/messages";
			String link = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/ERP_VENDOR/VendorLoginOtp.do?vendorId=" + EncryptionUtil.encrypt(vendorId);
			String templateName = templateId;
			String payload = "{"
							+ "\"messaging_product\": \"whatsapp\","
							+ "\"recipient_type\": \"individual\","
							+ "\"to\": \"" + phoneNumber + "\","
							+ "\"type\": \"template\","
							+ "\"template\": {"
							+ "	\"language\": {"
							+ "\"code\": \"en\""
							+ "},"
							+ "\"name\": \"" + templateName + "\","
							+ "\"components\": ["
							+ "{"
							+ "\"type\": \"body\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + amount + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + callId + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + coordinatorNumber + "/" + coordinatorEmail + "\""
							+ "}"
							+ "]"
							+ "}"
							+ "]"
							+ "}"
							+ "}";

			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("apikey", accessToken);
			conn.setRequestProperty("wanumber", phoneNumberId);
			conn.setDoOutput(true);

			// Send the payload
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = conn.getResponseCode();

			try (InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream()
					: conn.getErrorStream()) {
				String responseJson = new BufferedReader(new InputStreamReader(responseStream)).lines()
						.collect(Collectors.joining("\n"));
			}

			conn.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendPaymentSuccessfulMessageForMultipeCallIds(String phoneNumber, String templateId, String vendorId, String amount, String callId, String coordinatorEmail, String coordinatorNumber,  HttpServletRequest request, HttpServletResponse response) {
		try {

			DBConnectionPool.getConnection();
	        String otp = generateOTP(4);

	        // Step 2: Calculate expiry time (5 minutes from now)
	        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes in milliseconds


	        // Step 3: Store OTP and expiry time in session
	        HttpSession session = request.getSession();
	        session.setAttribute("otp", otp);
	        session.setAttribute("otpExpiryTime", expiryTime);

			// WhatsApp API URL
			String phoneNumberId = "918879112211";
			String accessToken = "b42b413d-5017-11ef-ad4f-92672d2d0c2d";
			String apiUrl = "https://partners.pinbot.ai/v2/messages";
			String link = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/ERP_VENDOR/VendorLoginOtp.do?vendorId=" + EncryptionUtil.encrypt(vendorId);
			String templateName = templateId;
			String payload = "{"
							+ "\"messaging_product\": \"whatsapp\","
							+ "\"recipient_type\": \"individual\","
							+ "\"to\": \"" + phoneNumber + "\","
							+ "\"type\": \"template\","
							+ "\"template\": {"
							+ "	\"language\": {"
							+ "\"code\": \"en\""
							+ "},"
							+ "\"name\": \"" + templateName + "\","
							+ "\"components\": ["
							+ "{"
							+ "\"type\": \"body\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + amount + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + callId + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + coordinatorNumber + "/" + coordinatorEmail + "\""
							+ "}"
							+ "]"
							+ "}"
							+ "]"
							+ "}"
							+ "}";

			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("apikey", accessToken);
			conn.setRequestProperty("wanumber", phoneNumberId);
			conn.setDoOutput(true);

			// Send the payload
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = conn.getResponseCode();

			try (InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream()
					: conn.getErrorStream()) {
				String responseJson = new BufferedReader(new InputStreamReader(responseStream)).lines()
						.collect(Collectors.joining("\n"));
			}

			conn.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendPaymentApprovalMessage(String phoneNumber, String templateId, String vendorId, String amount, String callId, String callDate, String clientName, String comment, String coordinatorEmail, String coordinatorNumber,  HttpServletRequest request, HttpServletResponse response) {
		try {

			Connection connection = DBConnectionPool.getConnection();
	        String otp = generateOTP(4);

	        // Step 2: Calculate expiry time (5 minutes from now)
	        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes in milliseconds


	        // Step 3: Store OTP and expiry time in session
	        HttpSession session = request.getSession();
	        session.setAttribute("otp", otp);
	        session.setAttribute("otpExpiryTime", expiryTime);

			// WhatsApp API URL
			String phoneNumberId = "918879112211";
			String accessToken = "b42b413d-5017-11ef-ad4f-92672d2d0c2d";
			String apiUrl = "https://partners.pinbot.ai/v2/messages";
			String link = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/ERP_VENDOR/VendorLoginOtp.do?vendorId=" + EncryptionUtil.encrypt(vendorId);
			String templateName = templateId;
			String payload = "{"
							+ "\"messaging_product\": \"whatsapp\","
							+ "\"recipient_type\": \"individual\","
							+ "\"to\": \"" + phoneNumber + "\","
							+ "\"type\": \"template\","
							+ "\"template\": {"
							+ "	\"language\": {"
							+ "\"code\": \"en\""
							+ "},"
							+ "\"name\": \"" + templateName + "\","
							+ "\"components\": ["
							+ "{"
							+ "\"type\": \"body\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + callId + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + callDate + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + clientName + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + coordinatorNumber + "/" + coordinatorEmail + "\""
							+ "}"
							+ "]"
							+ "}"
							+ "]"
							+ "}"
							+ "}";

			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("apikey", accessToken);
			conn.setRequestProperty("wanumber", phoneNumberId);
			conn.setDoOutput(true);

			// Send the payload
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = conn.getResponseCode();

			try (InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream()
					: conn.getErrorStream()) {
				String responseJson = new BufferedReader(new InputStreamReader(responseStream)).lines()
						.collect(Collectors.joining("\n"));
			}

			conn.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Courier login base URL.
	 * TODO: Change to production URL before deploying:
	 *       "https://crm.powerpointcart.com/ERP_VENDOR"
	 */
	private static final String COURIER_BASE_URL = "http://localhost:8085/replacement_management_war_exploded";

	/** Sends courier login credentials (passcode + link) via WhatsApp. */
	public static String sendCourierLoginMessage(String phoneNumber, String templateId, String courierId, String passcode) {
		String message = "";
		try {
			String phoneNumberId = "918879112211";
			String accessToken = "ee9a9362-c373-11f0-98fc-02c8a5e042bd";
			String apiUrl = "https://partnersv1.pinbot.ai/v3/405353099321184/messages";

			String link = COURIER_BASE_URL + "/CourierLoginOtp.do?courierId=" + EncryptionUtil.encrypt(courierId);

			String templateName = templateId;
			String payload = "{"
							+ "\"messaging_product\": \"whatsapp\","
							+ "\"recipient_type\": \"individual\","
							+ "\"to\": \"" + phoneNumber + "\","
							+ "\"type\": \"template\","
							+ "\"template\": {"
							+ "	\"language\": {"
							+ "\"code\": \"en\""
							+ "},"
							+ "\"name\": \"" + templateName + "\","
							+ "\"components\": ["
							+ "{"
							+ "\"type\": \"body\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + passcode + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + link + "\""
							+ "}"
							+ "]"
							+ "}"
							+ "]"
							+ "}"
							+ "}";

			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("apikey", accessToken);
			conn.setRequestProperty("wanumber", phoneNumberId);
			conn.setDoOutput(true);

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = conn.getResponseCode();
			String responseJson = "";

			try (InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream()
					: conn.getErrorStream()) {
				responseJson = new BufferedReader(new InputStreamReader(responseStream)).lines()
						.collect(Collectors.joining("\n"));
			}

			System.out.println("[COURIER WhatsApp] Status: " + status + " | Phone: " + phoneNumber);
			System.out.println("[COURIER WhatsApp] Passcode: " + passcode);
			System.out.println("[COURIER WhatsApp] Link: " + link);
			System.out.println("[COURIER WhatsApp] Response: " + responseJson);

			if (status == HttpURLConnection.HTTP_OK) {
				message = "Message is submitted!";
			} else {
				message = "Failed to submit the message. HTTP Status Code: " + status + " Response: " + responseJson;
			}

			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			message = "Error sending WhatsApp message: " + e.getMessage();
			System.out.println("[COURIER WhatsApp] ERROR: " + e.getMessage());
		}
		return message;
	}

	/** Sends courier forgot-password OTP via WhatsApp. */
	public static String sendCourierOtpMessage(String phoneNumber, String templateId, String courierId, String otp) {
		String message = "";
		try {
			String phoneNumberId = "918879112211";
			String accessToken = "ee9a9362-c373-11f0-98fc-02c8a5e042bd";
			String apiUrl = "https://partnersv1.pinbot.ai/v3/405353099321184/messages";

			String link = COURIER_BASE_URL + "/CourierLoginOtp.do?courierId=" + EncryptionUtil.encrypt(courierId);

			String templateName = templateId;
			String payload = "{"
							+ "\"messaging_product\": \"whatsapp\","
							+ "\"recipient_type\": \"individual\","
							+ "\"to\": \"" + phoneNumber + "\","
							+ "\"type\": \"template\","
							+ "\"template\": {"
							+ "	\"language\": {"
							+ "\"code\": \"en\""
							+ "},"
							+ "\"name\": \"" + templateName + "\","
							+ "\"components\": ["
							+ "{"
							+ "\"type\": \"body\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + otp + "\""
							+ "},"
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + link + "\""
							+ "}"
							+ "]"
							+ "}"
							+ "]"
							+ "}"
							+ "}";

			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("apikey", accessToken);
			conn.setRequestProperty("wanumber", phoneNumberId);
			conn.setDoOutput(true);

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = conn.getResponseCode();
			String responseJson = "";

			try (InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream()
					: conn.getErrorStream()) {
				responseJson = new BufferedReader(new InputStreamReader(responseStream)).lines()
						.collect(Collectors.joining("\n"));
			}

			System.out.println("[COURIER OTP WhatsApp] Status: " + status + " | Phone: " + phoneNumber);
			System.out.println("[COURIER OTP WhatsApp] OTP: " + otp);
			System.out.println("[COURIER OTP WhatsApp] Link: " + link);
			System.out.println("[COURIER OTP WhatsApp] Response: " + responseJson);

			if (status == HttpURLConnection.HTTP_OK) {
				message = "Message is submitted!";
			} else {
				message = "Failed to submit the message. HTTP Status Code: " + status + " Response: " + responseJson;
			}

			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			message = "Error sending WhatsApp message: " + e.getMessage();
			System.out.println("[COURIER OTP WhatsApp] ERROR: " + e.getMessage());
		}
		return message;
	}


	private static String generateOTP(int length) {
		String digits = "0123456789";
		Random random = new Random();
		StringBuilder otp = new StringBuilder();
		for (int i = 0; i < length; i++) {
			otp.append(digits.charAt(random.nextInt(digits.length())));
		}
		return otp.toString();
	}

	/**
	 * Sends cartridge pickup OTP via WhatsApp.
	 * Template: "Dear Customer, we are collecting {pickupQty} unused cartridges against the confirmed {orderQty}.
	 *            Please provide OTP {OTP} to our pickup executive for confirmation."
	 *
	 * @param con        JDBC connection for logging
	 * @param mobileno   target mobile of customer (with 91 prefix)
	 * @param templateName WhatsApp template name
	 * @param orderqty   unused cartridges qty ordered during pullback call
	 * @param pickupqty  pickup qty entered by pickup executive
	 * @param otp        generated OTP for validation
	 * @return success or error message
	 */
	public String sendCartPickupOTP(Connection con, String mobileno, String templateName,
									String orderqty, String pickupqty, String otp) {
		String message = "";
		try {
			String phoneNumberId = "918879112211";
			String accessToken = "ee9a9362-c373-11f0-98fc-02c8a5e042bd";
			String apiUrl = "https://partnersv1.pinbot.ai/v3/405353099321184/messages";

			// Template otp_verification: body {{1}} = OTP, button[0] url param = OTP
			String payload = "{"
							+ "\"messaging_product\": \"whatsapp\","
							+ "\"recipient_type\": \"individual\","
							+ "\"to\": \"" + mobileno + "\","
							+ "\"type\": \"template\","
							+ "\"template\": {"
							+ "	\"language\": {"
							+ "\"code\": \"en\""
							+ "},"
							+ "\"name\": \"" + templateName + "\","
							+ "\"components\": ["
							+ "{"
							+ "\"type\": \"body\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + otp + "\""
							+ "}"
							+ "]"
							+ "},"
							+ "{"
							+ "\"type\": \"button\","
							+ "\"sub_type\": \"url\","
							+ "\"index\": \"0\","
							+ "\"parameters\": ["
							+ "{"
							+ "\"type\": \"text\","
							+ "\"text\": \"" + otp + "\""
							+ "}"
							+ "]"
							+ "}"
							+ "]"
							+ "}"
							+ "}";

			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("apikey", accessToken);
			conn.setRequestProperty("wanumber", phoneNumberId);
			conn.setDoOutput(true);

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = conn.getResponseCode();
			String responseJson = "";

			try (InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream()
					: conn.getErrorStream()) {
				responseJson = new BufferedReader(new InputStreamReader(responseStream)).lines()
						.collect(Collectors.joining("\n"));
			}

			System.out.println("[PICKUP OTP WhatsApp] Status: " + status + " | Phone: " + mobileno);
			System.out.println("[PICKUP OTP WhatsApp] OTP: " + otp + " | OrderQty: " + orderqty + " | PickupQty: " + pickupqty);
			System.out.println("[PICKUP OTP WhatsApp] Response: " + responseJson);

			if (status == HttpURLConnection.HTTP_OK) {
				message = "Message is submitted!";
			} else {
				message = "Failed to submit the message. HTTP Status Code: " + status + " Response: " + responseJson;
			}

			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			message = "Error sending WhatsApp message: " + e.getMessage();
			System.out.println("[PICKUP OTP WhatsApp] ERROR: " + e.getMessage());
		}
		return message;
	}

}
