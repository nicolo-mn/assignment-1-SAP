package sap.account.infrastructure;

import java.util.HashMap;
import java.util.logging.Logger;

import sap.common.exagonal.Adapter;
import sap.account.application.AccountNotFoundException;
import sap.account.application.AccountRepository;
import sap.account.domain.Account;

/**
 * 
 * A simple in-memory implementation of the AccountRepository - no persistence.
 * 
 */
@Adapter
public class InMemoryAccountRepository implements AccountRepository {
	static Logger logger = Logger.getLogger("[AccountRepo]");

	private HashMap<String, Account> userAccounts;
	
	public InMemoryAccountRepository() {
		userAccounts = new HashMap<>();
	}
	
	public void addAccount(Account account) {
		userAccounts.put(account.getId(), account);
	}
	
	@Override
	public Account getAccount(String userName) throws AccountNotFoundException {
		return userAccounts.get(userName);
	}

	public boolean isPresent(String userName) {
		return userAccounts.containsKey(userName);
	}
	
	/**
	 * 
	 * Authenticate
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	public boolean isValid(String userName, String password) {
		return (userAccounts.containsKey(userName) && userAccounts.get(userName).getPassword().equals(password));
	}

	
}
