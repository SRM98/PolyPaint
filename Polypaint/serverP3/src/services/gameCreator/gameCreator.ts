import { gameDB } from '../../database/gameDB';
import { Game } from "../../models/game.model";
import DrawingModeSelector from '../drawingMode/drawingModeSelector';

export default class GameCreator {

    public async createGame(socket: any, game: Game) {
        game = this.removeGameWhitespaces(game);
        this.validateGameInputs(game);
        DrawingModeSelector.applyMode(game.image, game.mode).then((res) => {
            game.image = res;
            game.word = game.word.toLowerCase();
            gameDB.addGame(game);
        });
    }

    private validateGameInputs(game: Game) {
        if (!game.clues)
            throw "Please enter clues"
        else if (game.clues.length < 1)
            throw "Please enter at least 1 clue"
        else if (isNaN(game.difficulty))
            throw "Please select the difficulty";
        else if (!game.word)
            throw "Please select a valid word to guess";
        else if (isNaN(game.mode))
            throw "Please select a valid drawing mode";
    }

    private removeGameWhitespaces(game: Game) {
        if (game.word && game.clues) {
            game.word = game.word.trim();
            for(let i = 0; i < game.clues.length; ++i)
                game.clues[i] = game.clues[i].trim();
        }
        return game;
    }
}