/*
 * Created on 2.6.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package is.idega.block.pki.business;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.idega.idegaweb.IWMainApplication;

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
	//public static String SERIAL_NUM_START = "SERIAL_NUM=";

	//String personalIdPattern = "SERIAL_NUM=([\\d]{10}+)";
	String personalIdPattern = "serialNumber=([\\d]{10}+)";
	
	public static String APPLICATION_PARAM_DN_PID_PATTERN="pki.pidpattern.dn";
	
	
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
		
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(request.getSession().getServletContext());
		String prop = iwma.getSettings().getProperty(APPLICATION_PARAM_DN_PID_PATTERN);
		if(prop!=null){
			setPersonalIdPattern(prop);
		}
		
		parseParameters(request);
	}
	
	public PKICertificateInfo(String clientDN,String clientDNCN) {
		setDN(clientDN);
		setCN(clientDNCN);
		parsePersonalId(clientDN);
	}

	/**
	 * <p>
	 * TODO tryggvil describe method init
	 * </p>
	 * @param request
	 */
	protected void parseParameters(HttpServletRequest request) {
		String clientDN = (String)request.getAttribute(SSL_CLIENT_S_DN);
		setDN(clientDN);
		String clientCN = (String)request.getAttribute(SSL_CLIENT_S_DN_CN);
		setCN(clientCN);
		debug("ClientDN",clientDN);
		debug("ClientDNCN",clientCN);
		parsePersonalId(request);
	}

	/**
	 * <p>
	 * TODO tryggvil describe method parsePersonalId
	 * </p>
	 */
	protected void parsePersonalId(HttpServletRequest request) {
		String clientDN = getDN();
		//only used for debug and testing when no certificate available
		if(request.getParameter("certtestpid")!=null){
			String certPid = request.getParameter("certpid");
			setPersonalId(certPid);
		}
		else{
			parsePersonalId(clientDN);
		}
	}


	/**
	 * <p>
	 * TODO tryggvil describe method parsePersonalId
	 * </p>
	 * @param clientDN
	 */
	private void parsePersonalId(String clientDN) {
		
		Pattern pattern = Pattern.compile(getPersonalIdPattern());;
		
		Matcher m = pattern.matcher(clientDN);
		//m.find();
		if(clientDN!=null && m.find()){
			String kt = m.group(1);
			setPersonalId(kt);
		}
		else{
			throw new RuntimeException("Match of personalId not found in client DN");
		}
		
		//int indexKtStart = clientDN.indexOf(findString);
		//indexKtStart += findString.length();
		//indexKtStart += 3;
		//String kt = clientDN.substring(indexKtStart,indexKtStart+10);
		
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
	
	/**
	 * <p>
	 * Test method
	 * </p>
	 * @param args
	 */
	public static void main(String args[]){
		
		String dn = "/C=IS/serialNumber=1011783159/OU=Profunarskilriki/CN=\\x00T\\x00r\\x00y\\x00g\\x00g\\x00v\\x00i\\x00 \\x00L\\x00\\xE1\\x00r\\x00u\\x00s\\x00s\\x00o\\x00n";
		String cn = null;		
		
		//String dn = "C=IS,SERIAL_NUM=1011783159,OU=Profunarskilriki,CN=";
		//String cn = null;
		
		PKICertificateInfo info = new PKICertificateInfo(dn,cn);
		System.out.println("info.getPersonalId(): "+info.getPersonalId());
		
		
	}
	
	public String getPersonalIdPattern() {
		return personalIdPattern;
	}

	
	public void setPersonalIdPattern(String personalIdPattern) {
		this.personalIdPattern = personalIdPattern;
	}

}
