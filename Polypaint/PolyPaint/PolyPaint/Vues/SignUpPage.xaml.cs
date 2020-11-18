using System.Text;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Win32;
using PolyPaint.Modeles;
using System.Net.Http;
using Newtonsoft.Json;
using System;
using System.Windows.Media.Imaging;
using System.IO;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Logique d'interaction pour SignUpPage.xaml
    /// </summary>
    public partial class SignUpPage : Page
    {

        private readonly string CREATE_ACCOUNT_URL = MainWindow.ServerIP + "account/signUp";
        private static readonly HttpClient client = new HttpClient();
        private DrawWindow drawWindow;
        private byte[] imageData;

        public SignUpPage()
        {
            InitializeComponent();
            imageData = null;
        }

        private void uploadImage(object sender, RoutedEventArgs e)
        {
        
            OpenFileDialog fileDialog = new OpenFileDialog();
            fileDialog.Title = "Select an image"; // Required file extension 
            fileDialog.Filter = "All supported graphics|*.jpg;*.jpeg;*.png|" +
                                "JPEG (*.jpg;*.jpeg)|*.jpg;*.jpeg|" +
                                "Portable Network Graphic (*.png)|*.png";

            if (fileDialog.ShowDialog() == true)
            {

                BitmapImage bitmapImage;
                bitmapImage = new BitmapImage();

                bitmapImage.BeginInit();

                bitmapImage.StreamSource = System.IO.File.OpenRead(fileDialog.FileName);

                bitmapImage.EndInit(); //now, the Position of the StreamSource is not in the begin of the stream.

                imageData = new byte[bitmapImage.StreamSource.Length];

                bitmapImage.StreamSource.Seek(0, System.IO.SeekOrigin.Begin); //very important, it should be set to the start of the stream
                bitmapImage.StreamSource.Read(imageData, 0, imageData.Length);
                uploadButton.Content = fileDialog.SafeFileName;

            }
        }

        private void openDrawingWindow(object sender, RoutedEventArgs e)
        {
            drawWindow = new DrawWindow();
            DrawingPage.instance.isDrawingAvatar = true;

            drawWindow.Show();

            drawWindow.drawingArea.saveBtn.Click += SaveBtn_Click;
        }

        private void SaveBtn_Click(object sender, RoutedEventArgs e)
        {
            this.imageData = drawWindow.drawingArea.getImageData();
            this.drawWindow.Close();
            DrawingPage.instance.isDrawingAvatar = false;
            SavedText.Visibility = Visibility.Visible;
            System.Windows.Threading.DispatcherTimer myDispatcherTimer = new System.Windows.Threading.DispatcherTimer();
            myDispatcherTimer.Interval = new TimeSpan(0, 0, 0, 0, 2000); // 3000 Milliseconds 
            myDispatcherTimer.Tick += new EventHandler(tmr_Tick);
            myDispatcherTimer.Start();
        }

        public void tmr_Tick(object o, EventArgs sender)
        {
            SavedText.Visibility = Visibility.Collapsed;
        }

        private async void createAccount(object sender, RoutedEventArgs e)
        {
            if (!checkPasswordMatches())
            {
                MessageBox.Show("Passwords do not match", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            Account.Instance.username = username.Text;
            Account.Instance.password = password.Password;
            Account.Instance.firstName = firstName.Text;
            Account.Instance.lastName = lastName.Text;
            Account.Instance.imageData = imageData;

            var json = JsonConvert.SerializeObject(Account.Instance);
            var stringContent = new StringContent(json, UnicodeEncoding.UTF8, "application/json");
            var response = await client.PostAsync(CREATE_ACCOUNT_URL, stringContent);

            string responseString = formatResponse(await response.Content.ReadAsStringAsync());
            if (responseString == "200")
            {
                //MessageBox.Show("Your account was successfully created !");
                Application.Current.MainWindow.Content = new LoginPage();
            }
            else
            {
                ErrorTextBlock.Text = responseString;
                ErrorTextBlock.Visibility = Visibility.Visible;
            }

        }

        private string formatResponse(string response)
        {
            char[] MyChar = { '"' };
            response = response.TrimStart(MyChar);
            response = response.TrimEnd(MyChar);
            return response;
        }

        private void Back(object sender, RoutedEventArgs e)
        {
            Application.Current.MainWindow.Content = new LoginPage();
        }

        private bool checkPasswordMatches()
        {
            if (password.Password == confirmPassword.Password)
            {
                return true;
            }
            return false;
        }

    }
}
