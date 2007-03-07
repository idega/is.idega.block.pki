/**
 * 
 */
package is.idega.block.pki.business;


/**
 * <p>
 * 
 * </p>
 *  Last modified: $Date: 2007/03/07 19:19:46 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.2 $
 */
public class FirstCertificateAuthenticationException extends Exception {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 7291713872294680345L;
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
