import Account from "../../models/account.model";
import { accountDB } from "../../database/accountDB";
import HttpStatus from "../../models/HttpStatus";

const MIN_PASSWORD_LENGTH = 6;
const MIN_USERNAME_LENGTH = 3;
const MAX_USERNAME_LENGTH = 10;

export default class AccountManager {
    public constructor() {
    }

    public async editFirstName(username: String, newFirstName: String) {
        newFirstName = newFirstName.trim();
        await accountDB.editAccountFirstName(username, newFirstName);
        return HttpStatus.OK;
    }

    public async editLastName(username: String, newLastName: String) {
        newLastName = newLastName.trim();
        await accountDB.editAccountLastName(username, newLastName);
        return HttpStatus.OK;
    }

    public async editUsername(oldUsername: String, newUsername: String) {
        newUsername = newUsername.trim();
        if (!newUsername || 
            newUsername.length < MIN_USERNAME_LENGTH || 
            newUsername.length > MAX_USERNAME_LENGTH)
            throw "Your username must be between " + MIN_USERNAME_LENGTH 
                + " and " + MAX_USERNAME_LENGTH + " characters";
        else {
            await accountDB.editAccountUsername(oldUsername, newUsername);
            return HttpStatus.OK;
        }
    }

    public async editPassword(username: String, newPassword: String) {
        if (!newPassword || newPassword.length < MIN_PASSWORD_LENGTH)
            throw "Your password must be at least " + MIN_PASSWORD_LENGTH + " chars long";
        else {
            await accountDB.editAccountPassword(username, newPassword);
            return HttpStatus.OK;
        }
    }

    public async editAvatar(username: String, newAvatar: any[]) {
        await accountDB.editAccountAvatar(username, newAvatar);
        return HttpStatus.OK;
    }    
}