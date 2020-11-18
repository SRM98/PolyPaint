using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using PolyPaint.SocketCom;
using Newtonsoft.Json;
using System.Windows.Input;
using System.Windows.Input.StylusPlugIns;
using System.Windows.Ink;
using PolyPaint.Convertisseurs;
using PolyPaint.VuesModeles;

namespace PolyPaint.DrawCom
{
    class DrawMessage{
        public Object content{ get; set; }
        public string room { get; set; }
    }
    class EraseMessage
    {
        public int id { get; set; }
        public string room { get; set; }
    }
    public class DrawCommunication
    {
        public static int ZIndex = 0;

        public static bool CommunicationEnabled { get; set; }
        public string room { get; set; }

        private static SocketCommunication socketCommunication = SocketCommunication.Instance;
        public DrawCommunication(List<SubscriptionInfo> subscriptions)
        {
            CommunicationEnabled = true;
            this.room = "";
            subscriptions.ForEach((subscription) =>
            {
                socketCommunication.subscribeOnce(subscription.messageType_, subscription.method_);
            });
        }

        private string buildMessage(Object content)
        {
            return JsonConvert.SerializeObject(new DrawMessage() { content = content, room = room });
        }

        private string buildEraseMessage(int id)
        {
            return JsonConvert.SerializeObject(new EraseMessage() { id = id, room = room });
        }

        public void startStroke(RawStylusInput drawAttributes, DrawingAttributes attr, int width, int height)
        {
            if (CommunicationEnabled)
            {
                StrokeJSON stroke = InkCanvasConverter.StrokeToJSON(drawAttributes.GetStylusPoints(), attr, width, height, ZIndex);
                socketCommunication.emit("startStroke", buildMessage(stroke));
            }
        }

        public void eraseStroke(Stroke stroke)
        {
            if (CommunicationEnabled)
            {
                socketCommunication.emit("eraseStroke", buildEraseMessage((int)stroke.GetPropertyData(CustomInkCanvas.ZIndexGUID)));
            }
        }

        public void draw(RawStylusInput drawAttributes)
        {
            if (CommunicationEnabled)
            {
                PointXY[] drawMoves = InkCanvasConverter.StylusCollecionToArray(drawAttributes.GetStylusPoints());
                socketCommunication.emit("draw", buildMessage(drawMoves));
            }
        }

        public void erase(object drawAttributes)
        {
            if (CommunicationEnabled)
            {
                socketCommunication.emit("erase", JsonConvert.SerializeObject(drawAttributes));
            }
        }

        public static void stopPreview()
        {
            CommunicationEnabled = false;
            socketCommunication.emit("stopPreview", null );
        }
    }
}
