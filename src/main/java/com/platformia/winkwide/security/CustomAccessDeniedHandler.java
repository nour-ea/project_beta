package com.platformia.winkwide.security;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	protected Log logger = LogFactory.getLog(this.getClass());

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc)
			throws IOException, ServletException {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			logger.warn(
					"User: " + auth.getName() + " attempted to access the protected URL: " + request.getRequestURI());
					
			response.sendRedirect(request.getContextPath() + determineTargetUrl(auth));
			return;
		}

		response.sendRedirect(request.getContextPath() + "/accessDenied");
	}

	protected String determineTargetUrl(Authentication authentication) {
		boolean isMachine = false;
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority grantedAuthority : authorities) {
			if (grantedAuthority.getAuthority().equals("ROLE_MACHINE")) {
				isMachine = true;
				break;
			}
		}

		if (isMachine) {
			return "/403";
		} else {
			return "/portal/403";
		}
	}

}
