using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using PolyPaint.SocketCom;
using Newtonsoft.Json;

namespace PolyPaint.ChatComm
{
    class ChatCommunication
    {
        private SocketCommunication socketCommunication;

        public ChatCommunication(List<SubscriptionInfo> subscriptions)
        {
            socketCommunication = SocketCommunication.Instance;
            subscriptions.ForEach((subscription) =>
            {
                socketCommunication.subscribeOnce(subscription.messageType_, subscription.method_);
            });
        }

        public void sendMessage(MessageInfo messageInfo)
        {
            socketCommunication.emit("send", JsonConvert.SerializeObject(messageInfo));
        }
    }
}
