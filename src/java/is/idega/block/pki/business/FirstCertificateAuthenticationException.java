/**
 * 
 */
package is.idega.block.pki.business;


/**
 * <p>
 * 
 * </p>
 *  Last modified: $Date: 2007/03/06 23:29:40 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.1 $
 */
public class FirstCertificateAuthenticationException extends Exception {
	
	String personalId;
	
	public FirstCertificateAuthenticationException(String message,String personalId){
		super(message);
		setPersonalId(personalId);
	}

	
	public String getPersonalId() {
		return personalId;
	}

	
	public void setPersonalId(String personalId) {
		this.personalId = personalId;
	}
	
}
