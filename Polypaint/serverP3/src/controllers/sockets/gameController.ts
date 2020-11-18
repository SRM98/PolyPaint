import {
    ConnectedSocket, MessageBody,
    OnMessage, SocketController
} from "socket-controllers";
import GameCreator from '../../services/gameCreator/gameCreator';
import SvgToJson from '../../services/svgToJson/svgToJson';
import { Game } from '../../models/game.model';
import VirtuaDrawing from '../../services/virtualDrawing/virtualDrawing';
import { io } from "./sockets";
import { drawingIntervalMap } from '../../services/virtualDrawing/drawingIntervalsMap';
import DrawingModeSelector from '../../services/drawingMode/drawingModeSelector';
import { image_search } from "duckduckgo-images-api";
var request = require('request');


@SocketController()
export default class GameController {
    private gameCreator: GameCreator;
    private virtuaDrawing: VirtuaDrawing;
    public constructor() {
        this.virtuaDrawing = new VirtuaDrawing();
        this.gameCreator = new GameCreator();
    }

    @OnMessage("createGame")
    public createGame(@ConnectedSocket() socket: SocketIO.Socket, @MessageBody() game: any) {
        try {
            this.gameCreator.createGame(socket, game);
        } catch (e) {
            socket.emit("error", e);
        }
    }

    @OnMessage("searchImages")
    public searchImages(@ConnectedSocket() socket: SocketIO.Socket, @MessageBody() query: string) {
        image_search({ 
            query: query, 
            moderate : true,   
            iterations : 2 ,
            retries  : 2
        }).then(
            results => 
            {
                try {
                    
                    let thumbList = [];
                    let imagesList = [];
                    results.forEach(image => {                        
                        thumbList.push(image.thumbnail);
                        imagesList.push(image.image);
                    });
                    socket.emit("searchImages", { thumbList: thumbList, imageList: imagesList});
                } catch (e) {
                    console.log(e);
                }
        });
    }

    @OnMessage("convertSvgToJson")
    public convertSvgToJson(@ConnectedSocket() socket: SocketIO.Socket, @MessageBody() svg: string) {
        const svgConverter = new SvgToJson();
        
        try {
            request({ method: "POST", url:'http://18.216.84.168:4201/', body: { svg: svg}, 'Content-Type': 'application/json', 
                      json: true, timeout: 20000 }, function (error, response, body, request) {
                if (error){
                    console.log(error)
                    socket.emit("errorConvert", "problem converting")
                    console.log("errorConvert");
                }
                else if (response.statusCode == 200)
                    socket.emit("convertSvgToJson", body)
                else 
                    socket.emit("errorConvert", "problem converting")
            });
        } catch (e) {
            console.log(e);
        } 
    }

    @OnMessage("preview")
    public async preview(@ConnectedSocket() socket: SocketIO.Socket, @MessageBody() game: Game) {
        drawingIntervalMap.clearInterval(socket.id);
        socket.emit("startPreview");
        console.log(game.mode);
        game.image = await DrawingModeSelector.applyMode(game.image, game.mode);
        
        setTimeout(() => this.virtuaDrawing.draw(game, socket.id, io, socket), 300)
    }

    @OnMessage("stopPreview")
    public async stopPreview(@ConnectedSocket() socket: SocketIO.Socket, @MessageBody() game: Game) {        
        drawingIntervalMap.clearInterval(socket.id);
    }
}