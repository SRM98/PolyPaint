import { Difficulty } from "./game.model";

let timeToGuessHard = 120000;
let timeToGuessIntermediate = 80000;
let timeToGuessEasy = 50000;
let mapTimeToGuessDifficulty: Map<Difficulty, number> = new Map<Difficulty, number>();
mapTimeToGuessDifficulty.set(Difficulty.Easy, timeToGuessEasy);
mapTimeToGuessDifficulty.set(Difficulty.Intermediate, timeToGuessIntermediate);
mapTimeToGuessDifficulty.set(Difficulty.Hard, timeToGuessHard);

export function timeToGuessDifficultyMapper(diff: Difficulty) {
    return mapTimeToGuessDifficulty.get(diff);
};

export function bonusTimeByDifficulty(diff: Difficulty): number {
    switch(diff) {
        case Difficulty.Easy :         return 7;
        case Difficulty.Intermediate : return 5;
        case Difficulty.Hard :         return 3;
    }
};

export function triesDifficulty(diff: Difficulty): number {
    switch(diff) {
        case Difficulty.Easy :         return 4;
        case Difficulty.Intermediate : return 3;
        case Difficulty.Hard :         return 2;
    }
};