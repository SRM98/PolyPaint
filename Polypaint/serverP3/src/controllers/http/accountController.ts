import "reflect-metadata";
import { JsonController, Get, Post, Body, Param, OnUndefined, Put } from "routing-controllers";
import SignUp from "../../services/login/signup";
import SignIn from '../../services/login/signIn';
import { userSocketMap } from '../../services/userSocketsMap/userSocketsMap';
import { accountDB } from "../../database/accountDB";
import AccountManager from "../../services/account/accountManager";
import EditRequest from "../../models/editRequest";
import { updateAvatarUrl, removeImage } from "../../Image/imageManager"
import Account from '../../models/account.model';
import { io } from "../sockets/sockets";
import Message from "../../models/chat.model";
import { OnMessage, ConnectedSocket } from "socket-controllers";
import { avatarsDictionnary } from "../../Image/imageManager"

// documentation: https://codebrains.io/express-typescript-routing-controllers/

@JsonController("/account")
export default class AccountController { 
    
    private signUp: SignUp;
    private signIn: SignIn;
    private accountManager: AccountManager;
    public constructor() {
        this.signUp = new SignUp();
        this.signIn = new SignIn();
        this.accountManager = new AccountManager();
    }

    @Get("/")
    public test() {
        return "Hi";
    }

    @Get("/infos/:username")
    @OnUndefined(404)
    public async getAccountInfos(@Param("username") username: string) {
        try {     
            let account = await accountDB.getAccountByUsername(username);
            return {
                firstName: account.firstName,
                lastName: account.lastName,
                username: account.username,
                avatarUrl: avatarsDictionnary.get(username),
                stats: {
                    nbGames: account.stats.nbGames,
                    nbVictories: account.stats.nbVictories,
                    victoryPercentage: account.stats.victoryPercentage,
                    averageMatchesTime: account.stats.averageMatchesTime,
                    totalMatchesTime: account.stats.totalMatchesTime,
                }
            } 
        } catch (e) {
            return e;
        }
    }

    @Put("/editFirstName")
    public editAccountFirstName(@Body() editRequest: EditRequest) {
        try {   
            return this.accountManager.editFirstName(editRequest.username, editRequest.dataToEdit);
        } catch (e) {
            return e;
        }
    }

    @Put("/editLastName")
    public editAccountLastName(@Body() editRequest: EditRequest) {
        try {     
            return this.accountManager.editLastName(editRequest.username, editRequest.dataToEdit);
        } catch (e) {
            return e;
        }
    }

    @Put("/editUsername")
    public editAccountUsername(@Body() editRequest: EditRequest) {
        try {     
            return this.accountManager.editUsername(editRequest.username, editRequest.dataToEdit);
        } catch (e) {
            return e;
        }
    }

    @Put("/editPassword")
    public editAccountPassword(@Body() editRequest: EditRequest) {
        try {     
            return this.accountManager.editPassword(editRequest.username, editRequest.dataToEdit);
        } catch (e) {
            return e;
        }
    }

    @Put("/editAvatar")
    public editAccountAvatar(@Body() editRequest: EditRequest) {
        try {
            removeImage(editRequest.username);
            updateAvatarUrl(editRequest.username, editRequest.dataToEdit);   
            return this.accountManager.editAvatar(editRequest.username, editRequest.dataToEdit);
        } catch (e) {
            return e;
        }
    }

    @Post("/signUp")
    public gotSignUp(@Body() account: Account) {
        try {
            updateAvatarUrl(account.username, account.imageData);   
            return this.signUp.createAccount(account);      
        } catch (e) {
            return e;
        }
    }

    @Post("/signIn")
    public async gotSignIn(@Body() login: any) {
        const ok = await this.signIn.validateUser(login.username, login.password);
        userSocketMap.setSocketId(login.username, login.socketId);
        return ok;
    }
}