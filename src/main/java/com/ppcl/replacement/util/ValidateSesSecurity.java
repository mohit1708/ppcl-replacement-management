package com.ppcl.replacement.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Session validation utility.
 * Handles both internal admin session checks (existing method)
 * and courier-specific session management (login, validation, logout).
 */
public class ValidateSesSecurity
{
    /** Validates internal admin session (existing logic, not modified). */
    public static boolean validateRequest(HttpServletRequest request)
	{
    	boolean status=false;
    	try 
    	{
			String validate="NO", v1="";
			HttpSession ses=request.getSession(true);
			if((v1=(String)ses.getAttribute("4a#tH%%5A2s_Ps"))!=null){validate=v1;}
			if(validate.equals("6$RIy7Jy#-4e")){status = true;} else {status = false;}
		} 
    	catch (Exception e) 
    	{
			e.printStackTrace();
		}
		return status;
    }

    /* ---- Courier session management ---- */

    /** Creates a new courier session with 30-minute timeout. */
    public static void createCourierSession(HttpServletRequest request,
                                             int courierId,
                                             String courierName,
                                             String mobile) {
        HttpSession session = request.getSession(true);
        session.setAttribute("courierId", courierId);
        session.setAttribute("courierName", courierName);
        session.setAttribute("courierMobile", mobile);
        session.setAttribute("isCourierLoggedIn", true);
        session.setMaxInactiveInterval(30 * 60);
    }

    /** Returns true if the current session has an active courier login. */
    public static boolean isCourierSessionValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object loggedIn = session.getAttribute("isCourierLoggedIn");
        return Boolean.TRUE.equals(loggedIn);
    }

    /** Gets the logged-in courier's ID from session; returns 0 if not found. */
    public static int getCourierId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return 0;
        Object val = session.getAttribute("courierId");
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }

    /** Gets the logged-in courier's display name from session. */
    public static String getCourierName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (String) session.getAttribute("courierName");
    }

    /** Invalidates the courier session (used on logout). */
    public static void invalidateCourierSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
