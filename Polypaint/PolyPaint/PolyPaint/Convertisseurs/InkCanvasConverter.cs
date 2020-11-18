using System;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;

namespace PolyPaint.Convertisseurs
{
    public class StrokeAttributes
    {
        public double width { get; set; }
        public double height { get; set; }
        public System.Windows.Ink.StylusTip stylusType { get; set; }
        public int[] rgb { get; set; }
    }
    public class PointXY
    {
        public double x { get; set; }
        public double y { get; set; }
    }

    public class StrokeJSON
    {
        public StrokeAttributes drawAttributes { get; set; }
        public PointXY[] path { get; set; }
        public int imageWidth { get; set; } = 0;
        public int imageHeight { get; set; } = 0;
        public int zIndex { get; set; } = 0;
    }

    /// <summary>
    /// Permet de générer une couleur en fonction de la chaine passée en paramètre.
    /// Par exemple, pour chaque bouton d'un groupe d'options on compare son nom avec l'élément actif (sélectionné) du groupe.
    /// S'il y a correspondance, la bordure du bouton aura une teinte bleue, sinon elle sera transparente.
    /// Cela permet de mettre l'option sélectionnée dans un groupe d'options en évidence.
    /// </summary>
    class InkCanvasConverter
    {
        public static PointXY[] StylusCollecionToArray(StylusPointCollection stylusPoints)
        {
            PointXY[] points = new PointXY[stylusPoints.Count];
            for (int i = 0; i < stylusPoints.Count; i++)
            {
                points[i] = new PointXY() { x = stylusPoints[i].X, y = stylusPoints[i].Y };
            }
            return points;
        }

        public static StrokeJSON StrokeToJSON(StylusPointCollection points, DrawingAttributes attrib, int width, int height, int zIndex)
        {

            StrokeAttributes attributes = new StrokeAttributes()
            {
                width = attrib.Width,
                height = attrib.Height,
                stylusType = attrib.StylusTip,
                rgb = new int[] { (int)attrib.Color.R, (int)attrib.Color.G, (int)attrib.Color.B },
            };
            return new StrokeJSON() { drawAttributes = attributes, path = StylusCollecionToArray(points), imageHeight = height, imageWidth = width, zIndex = zIndex };
        }

        public static StrokeJSON StrokeToJSON(Stroke stroke, int width, int height, int zIndex)
        {
            return StrokeToJSON(stroke.StylusPoints, stroke.DrawingAttributes, width, height, zIndex);
        }

        public static Stroke JSONToStroke(StrokeJSON strokeJSON, double scaleFactorX, double scaleFactorY)
        {

            DrawingAttributes attributes = new DrawingAttributes();
            if (strokeJSON.drawAttributes.rgb == null)
                strokeJSON.drawAttributes.rgb = new int[] { 0, 0, 0 };
            attributes.Color = Color.FromRgb((byte)strokeJSON.drawAttributes.rgb[0], (byte)strokeJSON.drawAttributes.rgb[1], (byte)strokeJSON.drawAttributes.rgb[2]);
            attributes.Width = strokeJSON.drawAttributes.width;
            attributes.Height = strokeJSON.drawAttributes.height;
            attributes.StylusTip = strokeJSON.drawAttributes.stylusType;
            Stroke s = new Stroke(ArrayToStylusCollection(strokeJSON.path, scaleFactorX, scaleFactorY));
            s.DrawingAttributes = attributes;

            return s;
        }

        public static StylusPointCollection ArrayToStylusCollection(PointXY[] points, double scaleFactorX, double scaleFactorY)
        {

            StylusPointCollection PointCol = new StylusPointCollection();
            if (points == null)
                return PointCol;
            for (int i = 0; i < points.Length; i++)
            {
                if (points[i] != null)
                    PointCol.Add(new StylusPoint(points[i].x * scaleFactorX, points[i].y * scaleFactorY));
            }
            return PointCol;
        }

    }


}
