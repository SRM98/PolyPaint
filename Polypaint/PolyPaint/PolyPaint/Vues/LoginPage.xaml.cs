using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Net.Http;
using PolyPaint.Modeles;
using Newtonsoft.Json;
using System;
using System.Linq;
using System.Windows.Input;
using PolyPaint.SocketCom;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Logique d'interaction pour LoginPage.xaml
    /// </summary>
    public partial class LoginPage : Page
    {

        private readonly string SIGN_IN_URL = MainWindow.ServerIP + "account/signIn";
        private static readonly HttpClient client = new HttpClient();

        public LoginPage()
        {
            InitializeComponent();
            if (Account.Instance.username != "" && Account.Instance.password != "")
            {
                username.Text = Account.Instance.username;
                password.Password = Account.Instance.password;
                signIn(null, null);
            }
            MainWindow mainWindow = Application.Current.Windows.OfType<MainWindow>().FirstOrDefault();
            mainWindow.WindowState = WindowState.Normal;
            mainWindow.WindowStyle = WindowStyle.SingleBorderWindow;
            mainWindow.SizeToContent = SizeToContent.WidthAndHeight;
        }

        private async void signIn(object sender, RoutedEventArgs e)
        {
            if (SocketCommunication.Instance.socketId == "")
                return;
            Credentials credentials = new Credentials(username.Text.ToString(), password.Password.ToString(), SocketCommunication.Instance.socketId);

            var json = JsonConvert.SerializeObject(credentials);
            var stringContent = new StringContent(json, UnicodeEncoding.UTF8, "application/json");
            var response = await client.PostAsync(SIGN_IN_URL, stringContent);

            string responseString = formatResponse(await response.Content.ReadAsStringAsync());

            if (responseString == "200")
            {

                Account.Instance.username = username.Text.ToString();
                Account.Instance.password = password.Password.ToString();
                Rooms.instance = new Rooms();
                Application.Current.MainWindow.Content = new Lobby();

            }
            else
            {
                ErrorTextBlock.Text = responseString;
                ErrorTextBlock.Visibility = Visibility.Visible;
            }
        }

        private void OnKeyDownHandler(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Return)
            {
                signIn(sender, e);
            }
        }

        private void goCreateAccount(object sender, RoutedEventArgs e)
        {
            Application.Current.MainWindow.Content = new SignUpPage();
        }

        private string formatResponse(string response)
        {
            char[] MyChar = { '"' };
            response = response.TrimStart(MyChar);
            response = response.TrimEnd(MyChar);
            return response;
        }
    }
}
