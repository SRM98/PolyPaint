class DrawingIntervalMap {
    private intervalsMap: Map<string, any>;
    public constructor() {
        this.intervalsMap = new Map<string, string>();
    }
    public setRoomInterval(room: string, interval) {
        this.intervalsMap.set(room, interval);
    }
    public clearInterval(room) {
        const interval = this.intervalsMap.get(room);
        if (interval != null)
            clearInterval(interval);
        this.intervalsMap.delete(room);
    }
}

export let drawingIntervalMap = new DrawingIntervalMap();