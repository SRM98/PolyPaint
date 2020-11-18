using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using PolyPaint.SocketCom;
using Newtonsoft.Json;

namespace PolyPaint.ChatComm
{
    class DrawCommunication
    {
        private SocketCommunication socketCommunication;
        public DrawCommunication(List<SubscriptionInfo> subscriptions)
        {
            socketCommunication = SocketCommunication.Instance;
            subscriptions.ForEach((subscription) =>
            {
                socketCommunication.subscribe(subscription.messageType_, subscription.method_);
            });
        }
        public void draw(object drawAttributes)
        {
            socketCommunication.emit("draw", JsonConvert.SerializeObject(drawAttributes));
        }
        public void erase(object drawAttributes)
        {
            socketCommunication.emit("erase", JsonConvert.SerializeObject(drawAttributes));
        }
    }
}
