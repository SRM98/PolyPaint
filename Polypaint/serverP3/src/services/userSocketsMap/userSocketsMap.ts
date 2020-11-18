import { io } from '../../controllers/sockets/sockets';
export default class UserSocketMap {
    private socketMap: Map<string, string>;
    public constructor() {
        this.socketMap = new Map<string, string>();
    }
    public setSocketId(username: string, socket: string) {
        this.socketMap.set(username, socket);
    }
    public getSocketId(username): string {
        return this.socketMap.get(username);
    }
    public getUsernameBySocket(socketID): string {
        let username: string;
        this.socketMap.forEach((value, key) => {
            if(value === socketID)
                username = key;
        });
        
        if(username)
            return username;

        return "";
    }

    public removeSocketId(socketID: string): void{
        const username: string = this.getUsernameBySocket(socketID);
        this.socketMap.delete(username);
    }

    public isConnected(username): boolean {
        return this.socketMap.has(username) && this.socketMap.get(username) !== "" 
                && io.sockets.sockets[this.socketMap.get(username)] != undefined 
                && io.sockets.sockets[this.socketMap.get(username)].connected;
    }
}

export let userSocketMap = new UserSocketMap();