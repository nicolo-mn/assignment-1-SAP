package sap.account.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import sap.account.domain.Account;


/**
 * 
 * Implementation of the Account Service
 * 
 */
public class AccountServiceImpl implements AccountService {
	static Logger logger = Logger.getLogger("[TTT Account Service]");

	private AccountRepository accountRepository;
    
    public AccountServiceImpl(){
    }
    
    /**
     * 
     * Register a new user.
     * 
     * @param userName
     * @param password
     * @return
     * @throws AccountAlreadyPresentException
     */
	public Account registerUser(String userName, String password) throws AccountAlreadyPresentException {
		logger.log(Level.INFO, "Register User: " + userName + " " + password);		
		if (accountRepository.isPresent(userName)) {
			throw new AccountAlreadyPresentException();
		}
		var account = new Account(userName, password);
		accountRepository.addAccount(account);
		return account;
	}

	/**
	 * 
	 * Get account info
	 * 
	 */
	public Account getAccountInfo(String userName) throws AccountNotFoundException {
		logger.log(Level.INFO, "Get account info: " + userName);		
		if (!accountRepository.isPresent(userName)) {
			throw new AccountNotFoundException();
		}
		return accountRepository.getAccount(userName);
	}
    
		
	@Override
	public boolean isValidPassword(String userName, String password) throws AccountNotFoundException {
		logger.log(Level.INFO, "IsValid password " + userName + " - " + password);		
		if (!accountRepository.isPresent(userName)) {
			throw new AccountNotFoundException();
		}
		return accountRepository.getAccount(userName).getPassword().equals(password);
	}

    public void bindAccountRepository(AccountRepository repo) {
    	this.accountRepository = repo;
    }


}
