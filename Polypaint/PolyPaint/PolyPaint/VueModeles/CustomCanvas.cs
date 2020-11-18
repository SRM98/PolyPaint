using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Windows.Input.StylusPlugIns;
using System.Windows.Ink;
using PolyPaint.DrawCom;
using PolyPaint.Convertisseurs;
using PolyPaint.Modeles;
using Newtonsoft.Json;

namespace PolyPaint.VuesModeles
{
    /// <summary>
    /// Logique d'interaction pour CustomCanvas.xaml
    /// </summary>
    public class DrawEvents : StylusPlugIn
    {
        public static DrawingAttributes myDrawingAttributes;
        public DrawCommunication drawComm;
        public static int width;
        public static int height;
        public bool isDrawing { get; set; }
        public DrawEvents(List<SubscriptionInfo> subscriptions)
        {
            myDrawingAttributes = new DrawingAttributes();
            drawComm = new DrawCommunication(subscriptions);
            isDrawing = false;
        }

        protected override void OnStylusDown(RawStylusInput rawStylusInput)
        {
            if (Editeur.current == "efface_trait")
                return;
            isDrawing = true;
            // Call the base class before modifying the data.
            base.OnStylusDown(rawStylusInput);
            this.drawComm.startStroke(rawStylusInput, myDrawingAttributes, width, height);
            // Restrict the stylus input.
            Filter(rawStylusInput);
        }

        protected override void OnStylusMove(RawStylusInput rawStylusInput)
        {
            if (Editeur.current == "efface_trait")
                return;
            // Call the base class before modifying the data.
            base.OnStylusMove(rawStylusInput);

            this.drawComm.draw(rawStylusInput);

            // Restrict the stylus input.
            Filter(rawStylusInput);
        }

        protected override void OnStylusUp(RawStylusInput rawStylusInput)
        {
            // Call the base class before modifying the data.
            if (Editeur.current == "efface_trait")
                return;
            base.OnStylusUp(rawStylusInput);

            this.drawComm.draw(rawStylusInput);

            // Restrict the stylus input
            Filter(rawStylusInput);
            isDrawing = false;
        }

        public void OnStrokeErased(Stroke stroke)
        {
            this.drawComm.eraseStroke(stroke);
        }

        private void Filter(RawStylusInput rawStylusInput)
        {
        }
    }

    public class CustomInkCanvas : InkCanvas
    {
        public static readonly Guid ZIndexGUID = new Guid("69696969-0420-0420-6969-123456789012");
        public DrawEvents events;
        Stroke currentStroke;
        private Scaling scaling = new Scaling(){ x=1 , y=1, isScaling=false};
        private class Scaling
        {
            public double y { get; set; }
            public double x { get; set; }
            public bool isScaling { get; set; }

        }


        public CustomInkCanvas()
            : base()
        {
            List<SubscriptionInfo> subscriptions = new List<SubscriptionInfo>();
            subscriptions.Add(new SubscriptionInfo("draw", onDraw));
            subscriptions.Add(new SubscriptionInfo("startStroke", onStartStroke));
            subscriptions.Add(new SubscriptionInfo("eraseStroke", onEraseStroke));
            events = new DrawEvents(subscriptions);
            this.StrokeErasing += event_StrokeErased;
            this.StrokeCollected += event_StrokeCollected;
            this.StylusPlugIns.Add(events);

        }
        void event_StrokeErased(object sender, InkCanvasStrokeErasingEventArgs e)
        {
            if (Color.Equals(e.Stroke.DrawingAttributes.Color, Color.FromArgb(255, 255, 255, 255)))
            {
                e.Cancel = true;
            } else
                events.OnStrokeErased(e.Stroke);
        }

        void event_StrokeCollected(object sender, InkCanvasStrokeCollectedEventArgs e)
        {
            e.Stroke.AddPropertyData(ZIndexGUID, DrawCommunication.ZIndex++);
        }

        public void setRoom(string room)
        {
            this.events.drawComm.room = room;
        }

        public StrokeJSON[] CanvasToJSON()
        {
            StrokeJSON[] json = new StrokeJSON[Strokes.Count];
            for (int i = 0; i < Strokes.Count; i++)
            {
                json[i] = InkCanvasConverter.StrokeToJSON(Strokes[i], (int)this.ActualWidth, (int)this.ActualHeight, i);
            }
            return json;
        }

        private void onDraw(object pointsJSON)
        {
            try
            {
                PointXY[] points = JsonConvert.DeserializeObject<PointXY[]>(pointsJSON.ToString()); 
                StylusPointCollection stylusCol = InkCanvasConverter.ArrayToStylusCollection(points, scaling.x, scaling.y);
                this.Dispatcher.Invoke(() =>
                {
                    currentStroke?.StylusPoints.Add(stylusCol);
                });
            } catch (Exception)
            {

            }

        }

        public void onStartStroke(object rawJSON)
        {
            this.Dispatcher.Invoke(() =>
            {
                StrokeJSON strokeJSON = JsonConvert.DeserializeObject<StrokeJSON>(rawJSON.ToString());
                if(strokeJSON.imageWidth != 0 && strokeJSON.imageHeight != 0)
                {
                    scaling = new Scaling() { x = this.ActualWidth / (double)strokeJSON.imageWidth, 
                                              y = this.ActualHeight / (double)strokeJSON.imageHeight, 
                                              isScaling = true };
                } else
                {
                    scaling = new Scaling() { x = 1, y = 1, isScaling = false };
                }

                currentStroke = InkCanvasConverter.JSONToStroke(strokeJSON, scaling.x, scaling.y);
                currentStroke.AddPropertyData(ZIndexGUID, strokeJSON.zIndex);
                for (int i = 0; i < this.Strokes.Count; i++)
                {
                    if ((int)this.Strokes.ElementAt(i).GetPropertyData(ZIndexGUID) > strokeJSON.zIndex)
                    {
                        this.Strokes.Insert(i, currentStroke);
                        return;
                    }
                }
                this.Strokes.Add(currentStroke);
            });
        }

        private void onEraseStroke(object pointsJSON)
        {
            EraseMessage eraseMessage = JsonConvert.DeserializeObject<EraseMessage>(pointsJSON.ToString());
            this.Dispatcher.Invoke(() =>
            {
                for (int i = 0; i < this.Strokes.Count; i++)
                {
                    if ((int)this.Strokes.ElementAt(i).GetPropertyData(ZIndexGUID) == eraseMessage.id)
                    {
                        this.Strokes.RemoveAt(i);
                        return;
                    }
                }
            });
        }
    }
}
