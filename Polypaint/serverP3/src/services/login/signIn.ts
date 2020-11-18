import { accountDB } from "../../database/accountDB";
import Account from "../../models/account.model";
import HttpStatus from "../../models/HttpStatus";
import { userSocketMap } from '../userSocketsMap/userSocketsMap';

export default class SignIn {
    public constructor() {
    }

    public async validateUser(username: String, password: String) : Promise<null | number> {
        // validate username is not already there
        const user: Account = await accountDB.getAccountByUsername(username);
        if (user == null) {
            throw "Account doesn't exist";
        }
        if (userSocketMap.isConnected(username)) {
            throw "User is already connected";
        }
        if (password == user.password) {
            return HttpStatus.OK;
        } else {
            throw "Wrong password";
        }
    }
}