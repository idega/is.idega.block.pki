<%@page import="com.idega.core.accesscontrol.business.LoginBusinessBean"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="com.idega.presentation.IWContext"%>
<%@page import="is.idega.block.pki.business.PKILoginBusinessBean"%>
<%
/*
//variable debug:
java.util.Enumeration headerNames = request.getHeaderNames();
while(headerNames.hasMoreElements()){
	String header = (String)headerNames.nextElement();
	String headerValue = request.getHeader(header);
	out.println("Header: "+header+"="+headerValue+"<br>\n");
}

java.util.Enumeration requestNames = request.getAttributeNames();
while(requestNames.hasMoreElements()){
	String reqAttr = (String)requestNames.nextElement();
	String reqAttrVal = request.getAttribute(reqAttr).toString();
	out.println("RequestAttribute: "+reqAttr+"="+reqAttrVal+"<br>\n");
}
java.util.Enumeration sessionNames = request.getSession().getAttributeNames();
while(sessionNames.hasMoreElements()){
	String sessAttr = (String) sessionNames.nextElement();
if(sessAttr!=null){
        Object oSessAttrVal = request.getAttribute(sessAttr);
        if(oSessAttrVal!=null){
	String sessAttrVal = oSessAttrVal.toString();
	out.println("SessionAttribute: "+sessAttr+"="+sessAttrVal+"<br>\n");
}
}
}
*/

String clientCN = (String)request.getAttribute("SSL_CLIENT_S_DN_CN");
String clientDN = (String)request.getAttribute("SSL_CLIENT_S_DN");
System.out.println("CertificateAuthentication ClientCN: "+clientCN);
System.out.println("CertificateAuthentication ClientDN: "+clientDN);
if(clientDN!=null){

	IWContext iwc = IWContext.getInstance();
	if(iwc!=null){
		//out.println("Innskraning tokst!");
		String findString = "SERIAL_NUM=";
		int indexKtStart = clientDN.indexOf(findString);
		indexKtStart += findString.length();
		//indexKtStart += 3;
		String kt = clientDN.substring(indexKtStart,indexKtStart+10);
		//out.println("Kt: "+kt);	
		
		LoginBusinessBean loginBusiness = new PKILoginBusinessBean();
		boolean loginSuccess = loginBusiness.logInByPersonalID(iwc,kt);
		if(loginSuccess){
			response.sendRedirect("/pages/notandi/sidanmin/");
		}
		else{
			out.println("Innskraning mistokst!");
		}
	}
}
else{
}

%>

<%!class PKILoginBusinessBean extends PKILoginBusinessBean{
	
	/**
	 * temp: same implementation as in superclass
	 * This method by default throws an exception if the user hasn't already gotten a login.
	 */
	public boolean logInByPersonalID(IWContext iwc, String personalID) throws Exception {
		return logInByPersonalID(iwc,personalID,false);
	}
	
}%>