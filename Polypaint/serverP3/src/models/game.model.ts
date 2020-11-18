export interface Game {
    word: string,
    clues: Array<string>,
    difficulty: Difficulty,
    mode: DrawingMode,
    image: any
}

export enum Difficulty {
    Easy = 0,
    Intermediate = 1,
    Hard = 2,
} 

export enum DrawingMode {
    Replicated = 0,
    CenteredGoingIn = 1,
    CenteredGoingOut = 2,
    PanoramiqueTop = 3,
    PanoramiqueBottom = 4,
    PanoramiqueRight = 5,
    PanoramiqueLeft = 6,
    Random = 7
} 