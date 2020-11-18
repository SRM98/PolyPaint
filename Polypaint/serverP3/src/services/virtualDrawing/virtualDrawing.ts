import { Game, Difficulty, DrawingMode } from '../../models/game.model';
import {timeToGuessDifficultyMapper} from '../../models/inmatchAttributs';
import RandomImageCreator from '../drawingMode/randomImageCreator';
import { drawingIntervalMap } from './drawingIntervalsMap';

const scaleFactor = 1000;

export default class VirtuaDrawing {
    public draw(gameOriginal: Game, roomId: string, io: SocketIO.Server, socket: SocketIO.Socket): any {
        let game = this.clone(gameOriginal);
        if (game.mode == DrawingMode.Random) {
            game.image = RandomImageCreator.applyRandomImage(game.image);
        }
        let nbOfPoints = game.image.length;
        for (let i = 0; i < game.image.length; ++i) {
            nbOfPoints += game.image[i].path.length;                
        }
        let currentStroke = {path: []};
        let currentStrokeIndex = -1;
        let sendStrokes: boolean = game.image.length > 50;
        console.log(game.difficulty);
        
        let intervalTime = timeToGuessDifficultyMapper(game.difficulty)/(nbOfPoints/4 * (sendStrokes ? 1 : 14) );
        console.log(intervalTime);
        
        const currentInterval = setInterval(() => {
            if (currentStroke.path.length === 0) {
                currentStrokeIndex++;
                if (currentStrokeIndex === game.image.length) {
                    clearInterval(currentInterval);
                } else {
                    let currentPoints = this.clone(game.image[currentStrokeIndex].path);
                    currentStroke = game.image[currentStrokeIndex];

                    if(!sendStrokes) {
                        currentStroke.path = [currentPoints[0]];
                        io.sockets.in(roomId).emit("startStroke", currentStroke);
                        currentStroke.path = currentPoints;
                    } else {
                        io.sockets.in(roomId).emit("startStroke", currentStroke);
                        currentStroke.path = [];
                    }
                }
            } else {
                let coordinates = [];
                for (let i = 0; i < game.image[currentStrokeIndex].path.length && i <= 1; i++) {
                    coordinates.push(game.image[currentStrokeIndex].path.shift());                    
                }
                io.sockets.in(roomId).emit("draw", coordinates);
            }
        }, intervalTime);
        drawingIntervalMap.setRoomInterval(roomId, currentInterval);
        return currentInterval;
    }

    private clone(object: any): any {
        if (!object) return; 
        return JSON.parse(JSON.stringify(object));
    }
}