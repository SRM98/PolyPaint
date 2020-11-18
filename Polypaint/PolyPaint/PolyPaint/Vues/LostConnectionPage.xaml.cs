using PolyPaint.Modeles;
using PolyPaint.SocketCom;
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

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Page2.xaml
    /// </summary>
    public partial class LostConnectionPage : Page
    {
        public LostConnectionPage()
        {
            InitializeComponent();
            SocketCommunication socket = SocketCommunication.Instance;
            Account.Instance = null;
            socket.subscribe("reconnect", reconnect);
        }

        void reconnect(object data)
        {
            this.Dispatcher.Invoke(() => Application.Current.MainWindow.Content = new LoginPage());
        }
    }
}
