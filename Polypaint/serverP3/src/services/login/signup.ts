import { accountDB } from "../../database/accountDB";
import Account from '../../models/account.model';
import HttpStatus from "../../models/HttpStatus";

const MIN_PASSWORD_LENGTH = 6;
const MIN_USERNAME_LENGTH = 3;
const MAX_USERNAME_LENGTH = 10;

export default class SignUp {
    public constructor() {
    }

    public async createAccount(account: Account) {
        // validate username is not already there
        account = this.removeAccountWhitespaces(account);
        this.validateAccountInputs(account);
        const user = await accountDB.getAccountByUsername(account.username);
        if (user == null) {
            accountDB.addAccount(account);
            return HttpStatus.OK;
        } else {
            throw "Username already exists";
        }
    }


    private validateAccountInputs(account: Account) {
        if (!account.firstName)
            throw "Please enter your first name"
        else if (!account.lastName)
            throw "Please enter your last name"
        else if (account.username.search(/ /) !== -1)
            throw "Your username must not contain any whitespaces";
        else if (!account.username || 
            account.username.length < MIN_USERNAME_LENGTH || 
            account.username.length > MAX_USERNAME_LENGTH)
            throw "Your username must be between " + MIN_USERNAME_LENGTH 
                + " and " + MAX_USERNAME_LENGTH + " characters";
        else if (!account.password || account.password.length < MIN_PASSWORD_LENGTH)
            throw "Your password must be at least " + MIN_PASSWORD_LENGTH + " chars long";
    }

    private removeAccountWhitespaces(account: Account) {
        if (account.firstName && account.password
            && account.username && account.lastName) {
                account.firstName = account.firstName.trim();
                account.lastName = account.lastName.trim();
                account.username = account.username.trim()
        }
        return account;
    }
}