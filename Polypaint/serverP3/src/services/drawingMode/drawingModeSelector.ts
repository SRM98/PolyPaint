import { DrawingMode } from "../../models/game.model";
import CenteredImageCreator from './centeredImageCreator';
import PanoramiqueImageCreator from "./panoramiqueImageCreator";

export default class DrawingModeSelector {

    public static async applyMode(svg: any, mode: DrawingMode) {
        switch (mode) {
            case DrawingMode.CenteredGoingIn:
                return await CenteredImageCreator.applyCenteredImageGoingIn(svg);

            case DrawingMode.CenteredGoingOut:
                return await CenteredImageCreator.applyCenteredImageGoingOut(svg);

            case DrawingMode.PanoramiqueTop:
                return await PanoramiqueImageCreator.applyPanoramiqueImageTop(svg);

            case DrawingMode.PanoramiqueBottom:
                return await PanoramiqueImageCreator.applyPanoramiqueImageBottom(svg);

            case DrawingMode.PanoramiqueLeft:
                return await PanoramiqueImageCreator.applyPanoramiqueImageLeft(svg);

            case DrawingMode.PanoramiqueRight:
                return await PanoramiqueImageCreator.applyPanoramiqueImageRight(svg);

            default:
                return svg;
        }
    }
}