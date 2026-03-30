package sap.account.application;

import sap.common.exagonal.InBoundPort;
import sap.account.domain.Account;

/**
 * 
 * Interface of the Account Service at the application layer
 * 
 */
@InBoundPort
public interface AccountService  {

	/**
     * 
     * Register a new user.
     * 
     * @param userName
     * @param password
     * @return
     * @throws AccountAlreadyPresentException
     */
	Account registerUser(String userName, String password) throws AccountAlreadyPresentException;

	/**
     * 
     * Get account info.
     * 
     * @param userName
     * @return
     * @throws AccountNotFoundException
     */
	Account getAccountInfo(String userName) throws AccountNotFoundException;
		
	
	/**
	 * 
	 * Check password validity
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws AccountNotFoundException
	 */
	boolean isValidPassword(String userName, String password) throws AccountNotFoundException;

    
}
