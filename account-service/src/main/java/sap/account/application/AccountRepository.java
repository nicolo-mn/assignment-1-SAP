package sap.account.application;

import sap.common.ddd.Repository;
import sap.common.exagonal.OutBoundPort;
import sap.account.domain.Account;

/**
 * 
 * Interface of account repository
 * 
 */
@OutBoundPort
public interface AccountRepository extends Repository {

	void addAccount(Account account);
	
	boolean isPresent(String userName);

	Account getAccount(String userName) throws AccountNotFoundException;
	
	boolean isValid(String userName, String password);
}
