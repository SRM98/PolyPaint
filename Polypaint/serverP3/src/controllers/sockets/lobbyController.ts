import {
    ConnectedSocket,
    OnMessage, SocketController, MessageBody
} from "socket-controllers";
import { userSocketMap } from '../../services/userSocketsMap/userSocketsMap';
import AccountVerification from "../../services/login/accountVerification";

@SocketController()
export default class LobbyController {
    private accountVerifiation: AccountVerification;
    public constructor() {
        this.accountVerifiation = new AccountVerification();
    }
    
    @OnMessage("checkFirstTimeUserFatClient")
    async checkFirstTimeUserFatClient(@ConnectedSocket() socket: SocketIO.Socket, @MessageBody() username: string) {   
        // for first time users (tutorial)
        this.accountVerifiation.isFirstTimeUserFatClient(username).then((isFirstTimeUser: boolean) =>{
            if(isFirstTimeUser) {
                socket.emit("isFirstTimeUserFatClient"); 
                this.accountVerifiation.uncheckFirstTimeUserFatClient(username);
            }
        });
    }

    @OnMessage("checkFirstTimeUserThinClient")
    async checkFirstTimeUserLightClient(@ConnectedSocket() socket: SocketIO.Socket, @MessageBody() username: string) {   
        this.accountVerifiation.isFirstTimeUserThinClient(username).then((isFirstTimeUser: boolean) =>{
            if(isFirstTimeUser) {
                socket.emit("isFirstTimeUserThinClient"); 
                this.accountVerifiation.uncheckFirstTimeUserThinClient(username);
            }
        });
    }
}