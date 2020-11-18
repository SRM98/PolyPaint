import * as parser from 'fast-xml-parser';
import { resolve } from 'dns';
let svgpath = require('svgpath');
const ptToPxScale = 4/3;

export default class SvgToJson {
    private linearize(svg: string) {function convertBezier(match) {
        function convertSingle(single) {
        
            function M(scalar, array1) {
                   for (let i = 0; i < array1.length; i++) {
                       array1[i] *= scalar;
                   }
                   return array1
            }
    
            function add(array1, array2, array3) {
                   for (let i = 0; i < array1.length; i++) {
                    array1[i] += array2[i] + array3[i];
                  }
                return array1
            }
    
            let positions = single.split(' ').map(Number);
            let P0 = [positions[0], positions[1]];
            let P1 = [positions[2], positions[3]];
            let P2 = [positions[4], positions[5]];
            let t = 0.5; 
            let P = add(M((1-t)*(1-t), P0), M(2*(1-t)*t, P1), M(t*t, P2) );
               return positions[0] + " " + positions[1] + " " + P[0] + " " + P[1] + " " + positions[4] + " " + positions[5];
        
            }
            
            match = match.replace('c', '');
            return match.replace(/((-?\d+\.?\d* ){5}-?\d)/g, convertSingle);    
        }        
        return svg.replace(/c((-?\d+\.?\d* ){6})((-?\d+\.?\d* ){5}\d)*/g, convertBezier);
    }

    public async parseXml(svg: string) : Promise<object> {
        return new Promise<object>((resolve, reject) => {
            let errorInterval = setTimeout(() => console.log("error") , 15000);
    
            let json = (parser.parse(svg, {
                ignoreAttributes : false,
            }, true)).svg;
            let jsonPath = json.g.path;
            let transform = json.g["@_transform"].split(' ');
            let translate = transform[0].match(/-?\d*\.\d*/g).map(Number);
            let scale = transform[1].match(/-?\d*\.\d*/g).map(Number);
        
            let finalJson = [];
            
            const width = Math.ceil(Number(json["@_width"].replace("pt", "")));
            const height = Math.ceil(Number(json["@_height"].replace("pt", "")))
            
    
            jsonPath = jsonPath.length ? jsonPath : [jsonPath];
            
            for (let i = 0 ; i < jsonPath.length; ++i) {
                let paths: string = jsonPath[i]["@_d"];
                
                paths = new svgpath(paths)
                    .abs()
                    .scale(scale[0], scale[1])
                    .translate(translate[0], translate[1])
                    .round(4)
                    .toString();
                
                let splittedPaths = paths.split('M');
                
                for (let j = 0; j < splittedPaths.length; ++j) {
                    let path = this.parseStroke(splittedPaths[j])
                        
                    if (path.length > 0)
                        finalJson.push({
                            imageWidth: width,
                            imageHeight: height,
                            path: path,
                            drawAttributes: { width: 3, height: 3, stylusType: 1, rbg: [0, 0, 0]},
                            zIndex: 0     
                        });
                }
            }
            clearInterval(errorInterval);
            resolve(finalJson);
        });
    }

    private parseStroke(path: string) {
        path = path.replace('C', ' ');
        path = path.replace(/[A-Z|a-z]/g, ' ');
        let pathArray = path.split(' ').map(Number);
        let points: any[] = [];
        let prevX = pathArray[0];
        let prevY = pathArray[1];
        points.push({ x : prevX, y : prevY});
        for (let i = 2; i < pathArray.length; i+=2) {
            if (pathArray[i] && pathArray[i+1] && (i%4 === 0 || (points[points.length - 1] -  pathArray[i+1] > 10) || (points[points.length - 2] -  pathArray[i] > 10)) )
                points.push({ 
                                x: pathArray[i], 
                                y: pathArray[i+1]
                            });
        }

        return points.filter((value, index) => index%4 === 0);;
    }
}