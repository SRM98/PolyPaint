using System.Text;
using System.Windows.Controls;
using PolyPaint.Modeles;
using System.Net.Http;
using Newtonsoft.Json;
using System;
using Newtonsoft.Json.Linq;
using System.Windows.Media.Imaging;
using System.IO;
using System.Threading.Tasks;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Logique d'interaction pour AccountInfos.xaml
    /// </summary>
    public partial class AccountInfos : Page
    {
        private readonly string GET_ACCOUNT_INFOS = MainWindow.ServerIP + "account/infos";
        private static readonly HttpClient client = new HttpClient();

        public AccountInfos()
        {
            InitializeComponent();
            getAccountInfos();
        }

        private async void getAccountInfos()
        {
            var response = await client.GetAsync(GET_ACCOUNT_INFOS + "/" + Account.Instance.username);
            string result = await response.Content.ReadAsStringAsync();
            Account.Instance = Newtonsoft.Json.JsonConvert.DeserializeObject<Account>(result);


            if (Account.Instance.avatarUrl != null)
            {
                Account.Instance.avatarUrl = MainWindow.ServerIP + Account.Instance.avatarUrl;
                Avatar.Source = new BitmapImage(new Uri(Account.Instance.avatarUrl));
            }
            this.DataContext = Account.Instance;
            NbGames.Text = Account.Instance.stats.nbGames.ToString();
            WinRate.Text = Math.Round(Account.Instance.stats.victoryPercentage, 2).ToString() + " %";
            AverageGameTime.Text = formatTime(Account.Instance.stats.averageMatchesTime);
            TotalGameTime.Text = formatTime(Account.Instance.stats.totalMatchesTime);
        }

        private string formatTime(double seconds)
        {
            TimeSpan time = TimeSpan.FromSeconds(seconds);
            string formattedTimeSpan = string.Format("{0:D2} hrs {1:D2} mins {2:D2} secs", time.Hours, time.Minutes, time.Seconds);
            return formattedTimeSpan;
        }

        private void openEditWindow(object sender, System.Windows.RoutedEventArgs e)
        {
            string editType = ((Button)sender).Tag.ToString();
            EditWindow editWindow = new EditWindow(editType);
            editWindow.Show();

            editWindow.Closed += EditWindow_Closed;
        }

        private void EditWindow_Closed(object sender, EventArgs e)
        {
            getAccountInfos();
        }
    }
}
