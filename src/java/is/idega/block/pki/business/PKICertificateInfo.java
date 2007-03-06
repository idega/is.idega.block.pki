/*
 * Created on 2.6.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package is.idega.block.pki.business;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gummi
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PKICertificateInfo{// extends LoggedOnInfo {
	
	private String CN;
	private String DN;
	private String personalId;
	private boolean debug=true;
	
	public static String SSL_CLIENT_S_DN_CN  = "SSL_CLIENT_S_DN_CN";
	public static String SSL_CLIENT_S_DN = "SSL_CLIENT_S_DN";	
	public static String SERIAL_NUM_START = "SERIAL_NUM=";
	
	/*
	public final static String KEY_PERSONAL_ID = "serialNumber";
	private final static String _nameKey = "CN";
	private final static String _organizationKey = "O";
	private final static String _countryKey = "C";
	private final static String givenNameKey = "givenName";
	private final static String SurNameKey = "SN";
	*/
	
	
	public String getPersonalId() {
		return personalId;
	}

	
	public void setPersonalId(String personalId) {
		this.personalId = personalId;
	}

	/**
	 * @param request
	 */
	public PKICertificateInfo(HttpServletRequest request) {
		parseParameters(request);
	}

	/**
	 * <p>
	 * TODO tryggvil describe method init
	 * </p>
	 * @param request
	 */
	protected void parseParameters(HttpServletRequest request) {
		String clientCN = (String)request.getAttribute(SSL_CLIENT_S_DN_CN);
		setCN(clientCN);
		String clientDN = (String)request.getAttribute(SSL_CLIENT_S_DN);
		setDN(clientDN);
		debug("ClientCN",clientCN);
		debug("ClientDN",clientDN);
		parsePersonalId(request);
	}

	/**
	 * <p>
	 * TODO tryggvil describe method parsePersonalId
	 * </p>
	 */
	protected void parsePersonalId(HttpServletRequest request) {
		String clientDN = getDN();
		if(clientDN!=null){
			String findString = SERIAL_NUM_START;
			int indexKtStart = clientDN.indexOf(findString);
			indexKtStart += findString.length();
			//indexKtStart += 3;
			String kt = clientDN.substring(indexKtStart,indexKtStart+10);
			setPersonalId(kt);
		}
		//only used for debug and testing when no certificate available
		else if(request.getParameter("certtestpid")!=null){
			String certPid = request.getParameter("certpid");
			setPersonalId(certPid);
		}
	}

	/**
	 * <p>
	 * TODO tryggvil describe method debug
	 * </p>
	 * @param string
	 * @param clientCN
	 */
	private void debug(String message, String info) {
		if(debug){
			System.out.println("PKICertificateInfo: "+message+": "+info);
		}
	}

	public String getCN() {
		return CN;
	}
	
	public void setCN(String cn) {
		CN = cn;
	}
	
	public String getDN() {
		return DN;
	}
	
	public void setDN(String dn) {
		DN = dn;
	}
	

}
