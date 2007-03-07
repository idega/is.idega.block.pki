/*
 * Created on 26.5.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package is.idega.block.pki.business;

import java.util.Collection;
import java.util.Iterator;
import javax.ejb.FinderException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginCreateException;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.user.data.User;
import com.idega.util.StringHandler;

/**
 * @author <a href="mailto:tryggvi@idega.is">Tryggvi Larusson</a>
 * 
 */
public class PKILoginBusinessBean extends LoginBusinessBean {

	//private final static String IW_BUNDLE_IDENTIFIER = "is.idega.block.pki";
	public final static String PKI_LOGIN_TYPE = "is-pki-stjr";
	//public final static String PKI_NBSEXCEPTION = "se-pki-nexus-nbsexception";
	//public final static String PKI_EXCEPTION = "se-pki-nexus-exception";

	public final static String IWEX_PKI_USR_NOT_REGISTERED = "IWEX_PKI_USR_NOT_REGISTERED";
	public final static String IWEX_USER_HAS_NO_ACCOUNT = "IWEX_USER_HAS_NO_ACCOUNT";

	//private final static String NBS_BANKID_LOGIN_RESULT = "nbs_bankid_login_result";
	private final static String PKI_LOGGEDONINFO = "is_pki_loggedoninfo";
	
	/** Names for objects stored in the servlet context or session. */
	//private final static String SERVER_FACTORY = "se.idega.block.pki.ServerFactory", SERVER = "se.idega.block.pki.Server", SERVLET_URI = "se.nexus.cbt.ServletURI";

	/**
	 * 
	 */
	public PKILoginBusinessBean() {
		super();
	}

	/**
	 * This method is invoked by the IWAuthenticator and tries to log in or log
	 * out the user depending on the request parameters.
	 */
	public boolean processRequest(HttpServletRequest request) throws IWException {
		
		if(!isLoggedOn(request)){
			try {
				return logInByCertificate(request);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/*
	public boolean actionPerformed(IWContext iwc) {
		NBSResult result = null;
		try {
			// Get the server object.
			NBSServerHttp server = this.getNBSServer(iwc);
			HttpMessage httpReq = new HttpMessage();
			ServletUtil.servletRequestToHttpMessage(iwc.getRequest(), httpReq);

			// No action specified means that a message
			// probably has been received.

			// Process the message.
			result = server.handleMessage(httpReq);

			// Interpret the result.
			int type = result.getType();
			switch (type) {
				case (NBSResult.TYPE_AUTH) :
					this.logOutBankID(iwc);
					PKILoggedOnInfo info = (PKILoggedOnInfo)createLoggedOnInfo(iwc);
					info.setNbsAuthResult((NBSAuthResult)result);
					this.setBankIDLoggedOnInfo(iwc, info);
					logInUser(iwc, result);
					
					break;
				case (NBSResult.TYPE_SIGN) :
					throw new Exception("Unexpected result: NBSResult = TYPE_SIGN");
				case (NBSResult.TYPE_MESSAGE) :
					throw new Exception("Unexpected result: NBSResult = TYPE_MESSAGE");
				default :
					throw new Exception("Unknown result");
			}
		} catch (NBSException mpse) {
			this.carryOnNBSException(iwc, mpse);
			//System.err.println(mpse.getMessage());
			//mpse.printStackTrace();
			//printErrorCode(res, mpse.getCode(), mpse.getMessage());
		} catch (Exception e) {
			this.carryOnException(iwc, e);
			//System.out.println("Exception:"+e.getMessage());
			//e.printStackTrace();
			//printErrorMessage(res, e.getMessage());
		}
		return true;
	}
	*/
	
	/**
	 * if requireExisitingLogin is true then this method throws an exception if the user hasn't already gotten a login, otherwise it will create a new bankId login
	 * @return LoginTable record to log on the system
	 */
	public LoginTable chooseLoginRecord(HttpServletRequest request, LoginTable[] loginRecords, User user,boolean requireExisitingLogin) throws Exception {
		LoginTable chosenRecord = null;
		if (loginRecords != null) {
			for (int i = 0; i < loginRecords.length; i++) {
				String type = loginRecords[i].getLoginType();
				if (type != null && type.equals(PKI_LOGIN_TYPE)) {
					chosenRecord = loginRecords[i];
				}
			}
		}

		if (chosenRecord == null) {
			boolean mayCreateNewLogin=false;
			if(!requireExisitingLogin){
				mayCreateNewLogin=true;
			}
			else{
				if(loginRecords.length > 0){
					mayCreateNewLogin=true;
				}
			}
			
			if(mayCreateNewLogin){
			//if (loginRecords.length > 0) {
				String newLogin = StringHandler.getRandomString(20);
				Integer userId = (Integer)user.getPrimaryKey();
				chosenRecord = LoginDBHandler.createLogin(userId.intValue(), newLogin, "noPassword");
				chosenRecord.setLoginType(PKILoginBusinessBean.PKI_LOGIN_TYPE);
				chosenRecord.store();
				return chosenRecord;
			} else {
				Exception e = new FirstCertificateAuthenticationException(IWEX_USER_HAS_NO_ACCOUNT,user.getPersonalID());
				//this.carryOnException(iwc, e);
				throw e;
			}

			//			try {

			//				throw new LoginCreateException("PKI login record could not be created");
			//			} catch (LoginCreateException e) {
			//				System.out.println(e.getMessage());
			//				e.printStackTrace();
			//				return null;
			//			}
		} else {
			return chosenRecord;
		}
	}
	
	
	public boolean logInByCertificate(HttpServletRequest request) throws Exception{
		boolean loginSuccessful = false;
		PKICertificateInfo lInfo = createLoggedOnInfo(request);

		String personalID =  lInfo.getPersonalId();
		//try {
			loginSuccessful = this.logInByPersonalID(request, personalID);

			System.out.println("PKILoginBusinessBean logInByCertificate: " + ((loginSuccessful) ? "successful" : "failed") + " for personalId : '" + personalID + "'");
			if (!loginSuccessful) {
				throw new FirstCertificateAuthenticationException(IWEX_USER_HAS_NO_ACCOUNT,personalID);
			}
			
			/*if (iwc.isParameterSet(IWAuthenticator.PARAMETER_REDIRECT_USER_TO_PRIMARY_GROUP_HOME_PAGE)){
				if(iwc.isLoggedOn()||LoginBusinessBean.isLogOnAction(iwc)) {
					Group prmg = iwc.getCurrentUser().getPrimaryGroup(); 
					if (prmg != null) {
						int homePageID = prmg.getHomePageID();
						if (homePageID > 0) {
							BuilderService builderService = BuilderServiceFactory.getBuilderService(iwc);
							HttpServletResponse response = iwc.getResponse();
							response.sendRedirect(builderService.getPageURI(homePageID));
						}
					}
				}
			}*/
			
		/*} catch (Exception ex) {
			this.carryOnException(request, ex);
			//System.out.println("idegaWeb Login failed for personalId : '" + personalID + "'");
			//ex.printStackTrace();

		}*/
		return loginSuccessful;
	}

	/*
	private boolean logInUser(IWContext iwc, NBSResult result) {
		boolean loginSuccessful = false;
		NBSAuthResult authResult = (NBSAuthResult)result;

		String personalIDKey = "serialNumber";
		String personalID = authResult.getSubjectAttributeValue(personalIDKey);
		try {
			loginSuccessful = this.logInByPersonalID(iwc, personalID);

			System.out.println("idegaWeb Login " + ((loginSuccessful) ? "successful" : "failed") + " for personalId : '" + personalID + "'");
			if (!loginSuccessful) {
				throw new Exception(IWEX_PKI_USR_NOT_REGISTERED + "#" + personalID + "#");
			}
			
			if (iwc.isParameterSet(IWAuthenticator.PARAMETER_REDIRECT_USER_TO_PRIMARY_GROUP_HOME_PAGE)){
				if(iwc.isLoggedOn()||LoginBusinessBean.isLogOnAction(iwc)) {
					Group prmg = iwc.getCurrentUser().getPrimaryGroup(); 
					if (prmg != null) {
						int homePageID = prmg.getHomePageID();
						if (homePageID > 0) {
							BuilderService builderService = BuilderServiceFactory.getBuilderService(iwc);
							HttpServletResponse response = iwc.getResponse();
							response.sendRedirect(builderService.getPageURI(homePageID));
						}
					}
				}
			}
			
		} catch (Exception ex) {
			this.carryOnException(iwc, ex);
			//System.out.println("idegaWeb Login failed for personalId : '" + personalID + "'");
			//ex.printStackTrace();

		}
		return loginSuccessful;
	}
	*/


	/*
	public static Exception getException(IWContext iwc) {
		return (Exception)iwc.getSessionAttribute(PKI_EXCEPTION);
	}*/

	public PKICertificateInfo createLoggedOnInfo(HttpServletRequest request) {
		PKICertificateInfo info = getPKILoggedOnInfo(request.getSession());
		if (info == null) {
			info = new PKICertificateInfo(request);
			setPKILoggedOnInfo(info, request.getSession());
		}
		return info;
	}

	
	/*public LoggedOnInfo getLoggedOnInfo(HttpSession session) {
		return (PKICertificateLoggedOnInfo)session.getAttribute(PKI_LOGGEDONINFO);
	}*/
	
	public PKICertificateInfo getPKILoggedOnInfo(HttpSession session) {
		//return (PKICertificateLoggedOnInfo)getLoggedOnInfo(session);
		return (PKICertificateInfo)session.getAttribute(PKI_LOGGEDONINFO);
	}
	
	/*public LoggedOnInfo getPKILoggedOnInfo(HttpSession session) {
		return (LoggedOnInfo)session.getAttribute(PKI_LOGGEDONINFO);
	}*/

	public void setPKILoggedOnInfo(PKICertificateInfo info, HttpSession session) {
		session.setAttribute(PKI_LOGGEDONINFO, info);
	}
	
	public void logOutPKI(HttpServletRequest request) {
		request.getSession().removeAttribute(PKI_LOGGEDONINFO);
	}

	public void logOut(HttpServletRequest request) throws Exception {
		super.logOut(request);
		this.logOutPKI(request);
	}

	/**
	 * temp: same implementation as in superclass
	 * This method by default throws an exception if the user hasn't already gotten a login.
	 */
	public boolean logInByPersonalID(HttpServletRequest request, String personalID) throws Exception {
		return logInByPersonalID(request,personalID,true);
	}
	
	/**
	 * if requireExisitingLogin is true then this method throws an exception if the user hasn't already gotten a login.
	 */
	public boolean logInByPersonalID(HttpServletRequest request, String personalID,boolean requireExistingLogin) throws Exception {
		boolean returner = false;
		try {
			
			IWApplicationContext iwc = IWMainApplication.getIWMainApplication(request.getSession().getServletContext()).getIWApplicationContext();
			com.idega.user.data.User user = getUserBusiness(iwc).getUser(personalID);
			//LoginTable[] login_table = (LoginTable[]) (com.idega.core.accesscontrol.data.LoginTableBMPBean.getStaticInstance()).findAllByColumn(com.idega.core.accesscontrol.data.LoginTableBMPBean.getColumnNameUserID(), user.getPrimaryKey().toString());

			Collection loginRecords = ((LoginTableHome)IDOLookup.getHome(LoginTable.class)).findLoginsForUser(user);
			LoginTable[] login_table = (LoginTable[])loginRecords.toArray(new LoginTable[loginRecords.size()]);


			LoginTable lTable = this.chooseLoginRecord(request, login_table, user,requireExistingLogin);
			if (lTable != null) {
				returner = logIn(request, lTable);
				if (returner) {
					onLoginSuccessful(request);
				}
			} else {
				try {
					throw new LoginCreateException("No record chosen");
				} catch (LoginCreateException e1) {
					e1.printStackTrace();
				}
			}

		} catch (FinderException e) {
			System.err.println("User with personalId:"+personalID+" not found in db.");
			returner = false;
		}
		return returner;

	}

	public static PKILoginBusinessBean createNBSLoginBusiness() {
		return new PKILoginBusinessBean();
	}

	public boolean hasPKILogin(User user){
		try {
			Collection loginRecords = ((LoginTableHome)IDOLookup.getHome(LoginTable.class)).findLoginsForUser(user);
			
			for (Iterator iter = loginRecords.iterator(); iter.hasNext();) {
				String type = ((LoginTable)iter.next()).getLoginType();
				if (type != null && type.equals(PKI_LOGIN_TYPE)) {
					return true;
				}
			}
		} catch (IDOLookupException e) {
			e.printStackTrace();
			return false;
		} catch (FinderException e) {
			e.printStackTrace();
			return false;
		}
		
		//LoginTable[] loginRecords = (LoginTable[]) (com.idega.core.accesscontrol.data.LoginTableBMPBean.getStaticInstance()).findAllByColumn(com.idega.core.accesscontrol.data.LoginTableBMPBean.getColumnNameUserID(), Integer.toString(userID));
		
//		if (loginRecords != null) {
//			for (int i = 0; i < loginRecords.length; i++) {
//				String type = loginRecords[i].getLoginType();
//				if (type != null && type.equals(PKI_LOGIN_TYPE)) {
//					return true;
//				}
//			}
//		}

		return false;
	}

}
