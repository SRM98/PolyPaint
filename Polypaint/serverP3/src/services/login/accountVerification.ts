import { accountDB } from "../../database/accountDB";

export default class AccountVerification {
    public constructor() {
    }

    public async isFirstTimeUserFatClient(username: String){
        try{
            return await accountDB.getAccountByUsername(username).then((user) => {
                console.log("firsttime: " + user);
                return user.firstTimeUserFatClient;
            });
        }
        catch(e){
            return e;
        }
    }

    public async isFirstTimeUserThinClient(username: String){
        try{
            return await accountDB.getAccountByUsername(username).then((user) => {
                return user.firstTimeUserThinClient;
            });
        }
        catch(e){
            return e;
        }
    }

    public async uncheckFirstTimeUserFatClient(username: string) {
        await accountDB.uncheckFirstTimeUserFatClient(username);
    }

    public async uncheckFirstTimeUserThinClient(username: string) {
        await accountDB.uncheckFirstTimeUserThinClient(username);
    }
}