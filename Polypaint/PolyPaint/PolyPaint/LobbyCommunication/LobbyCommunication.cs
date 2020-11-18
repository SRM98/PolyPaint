using PolyPaint.Modeles;
using PolyPaint.SocketCom;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PolyPaint.LobbyComm
{
    class LobbyCommunication
    {
        private SocketCommunication socketCommunication;
        public LobbyCommunication(List<SubscriptionInfo> subscriptions)
        {
            socketCommunication = SocketCommunication.Instance;
            subscriptions.ForEach((subscription) =>
            {
                socketCommunication.subscribe(subscription.messageType_, subscription.method_);
            });
        }

        public void checkFirstTimeUser()
        {
            socketCommunication.emit("checkFirstTimeUserFatClient", Account.Instance.username);
        }

    }
}
