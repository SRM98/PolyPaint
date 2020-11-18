import "reflect-metadata";
import Message from "../../models/chat.model";
let Filter = require("bad-words");
import * as frenchList from "french-badwords-list";

export class ChatFilter {
    private filter: any;
    public constructor(){
        this.filter = new Filter();
        this.filter.addWords(...frenchList.array);
    }

    public filterMessage(socket: SocketIO.Socket, message: Message) {
        let newMessage = this.filter.clean(message.text);
        if(message.text != newMessage && (message.sender.search(/(Ai Player)/) === -1) ) {
            socket.emit("badWord");
        }
        newMessage = newMessage.replace(/juju/gi, "Chef JuJu"); 
        newMessage = newMessage.replace(/julien/gi, "Chef JuJu"); 
        newMessage = newMessage.replace(/olivier/gi, "Grand Chef"); 
        newMessage = newMessage.replace(/oli/gi, "Grand Chef"); 
        newMessage = newMessage.replace(/seb[^a]/gi, "Chef Sebi"); 
        newMessage = newMessage.replace(/sebastien/gi, "Chef Sebi"); 
        newMessage = newMessage.replace(/sébastien/gi, "Chef Sebi"); 
        newMessage = newMessage.replace(/nikolai/gi, "Chef Niko"); 
        newMessage = newMessage.replace(/nik/gi, "Chef Niko"); 
        return newMessage;
    }
}