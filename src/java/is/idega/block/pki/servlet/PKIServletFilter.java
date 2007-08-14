package is.idega.block.pki.servlet;
import is.idega.block.pki.business.FirstCertificateAuthenticationException;
import is.idega.block.pki.business.PKILoginBusinessBean;
import is.idega.idegaweb.egov.citizen.presentation.CitizenAccountApplication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWURL;
import com.idega.servlet.filter.IWAuthenticator;
import com.idega.util.RequestUtil;


/**
 * 
 */
/**
 * <p>
 * Servlet filter that authenticates a user into the idegaWeb User system by a PKI Certificate.
 * </p>
 *  Last modified: $Date: 2007/08/14 12:39:01 $ by $Author: alexis $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.2 $
 */
public class PKIServletFilter extends IWAuthenticator {

	
	public static String APPLICATION_PARAM_REGISTRATION_PAGE_URI="pki.registrationpageuri";
	
	private String registrationPageBaseUrl = "/pages/nyskraning/";

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException,
			ServletException {
		
		HttpServletRequest request = (HttpServletRequest)srequest;
		HttpServletResponse response = (HttpServletResponse)sresponse;
		
		boolean didRedirect = doPKIAuthentication(request,response);
		if(!didRedirect){
			chain.doFilter(request, response);
		}
	}

	/**
	 * <p>
	 * Executes the authentication of the PKI Certificate
	 * </p>
	 * @param request
	 * @param response
	 */
	private boolean doPKIAuthentication(HttpServletRequest request, HttpServletResponse response) {
		
		PKILoginBusinessBean bean = new PKILoginBusinessBean();
		boolean success = false;
		try{
			success = bean.logInByCertificate(request);
			if(success){
				//Redirect to the user home page:
				boolean redirectToUserHomepage=true;
				return processRedirectsToUserHome(request, response, request.getSession(), bean, redirectToUserHomepage);
			}
			else{
				response.sendError(500);
			}
		}
		catch(FirstCertificateAuthenticationException ufl){
			try {
				
				String redirectUrl = getRegistrationRedirectUrl(request,ufl);
				
				response.sendRedirect(redirectUrl);
				return true;
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * <p>
	 * TODO tryggvil describe method getRegistrationRedirectUrl
	 * </p>
	 * @param request
	 * @param ufl
	 * @return
	 */
	protected String getRegistrationRedirectUrl(HttpServletRequest request, FirstCertificateAuthenticationException ufl) {

		String baseurl = getRegistrationPageBaseUrl();
		IWURL url = new IWURL(baseurl);
		
		String pidParameter = CitizenAccountApplication.SSN_KEY;
		String personalId = ufl.getPersonalId();
		url.addParameter(pidParameter, personalId);
		
		url.addParameter(CitizenAccountApplication.PARAMETER_HIDE_PERSONALID_INPUT, "true");
		url.addParameter(CitizenAccountApplication.PARAMETER_CREATE_LOGIN_AND_LETTER, "false");
		
		String thisUri = request.getRequestURI();
		url.addParameter(CitizenAccountApplication.PARAMETER_REDIRECT_URI, thisUri);
		
		return url.getFullURL();
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
	
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(config.getServletContext());
		
		String prop = iwma.getSettings().getProperty(APPLICATION_PARAM_REGISTRATION_PAGE_URI);
		if(prop!=null){
			setRegistrationPageBaseUrl(prop);
		}
		
	}

	
	public String getRegistrationPageBaseUrl() {
		return registrationPageBaseUrl;
	}

	
	public void setRegistrationPageBaseUrl(String registrationPageBaseUrl) {
		this.registrationPageBaseUrl = registrationPageBaseUrl;
	}
}
