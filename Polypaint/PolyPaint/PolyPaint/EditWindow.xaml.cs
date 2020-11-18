using PolyPaint.Modeles;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Net.Http;
using Newtonsoft.Json;
using Microsoft.Win32;
using System.Windows.Media.Imaging;
using System.IO;

namespace PolyPaint
{
    /// <summary>
    /// Logique d'interaction pour EditWindow.xaml
    /// </summary>
    public partial class EditWindow : Window
    {
        private readonly string EDIT_FIRSTNAME = MainWindow.ServerIP + "account/editFirstName";
        private readonly string EDIT_LASTNAME = MainWindow.ServerIP + "account/editLastName";
        private readonly string EDIT_USERNAME = MainWindow.ServerIP + "account/editUsername";
        private readonly string EDIT_PASSWORD = MainWindow.ServerIP + "account/editPassword";
        private readonly string EDIT_AVATAR = MainWindow.ServerIP + "account/editAvatar";

        private static readonly HttpClient client = new HttpClient();
        private string editType { get; set; }
        public object dataToEdit { get; set; }
        private DrawWindow drawWindow;
        public EditWindow(string editType = null)
        {
            InitializeComponent();
            this.editType = editType;
            switch(this.editType)
            {
                case "firstName":  DefaultInfos.Visibility = Visibility.Visible;
                                   EditingLabel.Content = "Edit your current first name :";
                                   this.dataToEdit = Account.Instance.firstName;
                                   break;

                case "lastName":   DefaultInfos.Visibility = Visibility.Visible;
                                   EditingLabel.Content = "Edit your current last name :";
                                   this.dataToEdit = Account.Instance.lastName;
                                   break;

                case "username":   DefaultInfos.Visibility = Visibility.Visible;
                                   EditingLabel.Content = "Edit your current username :";
                                   this.dataToEdit = Account.Instance.username;
                                   break;

                case "password":   PasswordInfo.Visibility = Visibility.Visible;
                                   break;
                
                case "avatar":     Image.Visibility = Visibility.Visible;
                                    if (Account.Instance.avatarUrl != null)
                                            Avatar.Source = Avatar.Source = new BitmapImage(new Uri(Account.Instance.avatarUrl));
                                    break;

                default: break;
            }
            this.DataContext = this;
        }

        private async void saveChanges(object sender, RoutedEventArgs e)
        {
            HttpResponseMessage response;

            switch (this.editType)
            {
                case "firstName":
                    response = await client.PutAsync(EDIT_FIRSTNAME, createRequestContent(this.dataToEdit));
                    break;

                case "lastName":
                    response = await client.PutAsync(EDIT_LASTNAME, createRequestContent(this.dataToEdit));
                    break;

                case "username":
                    response = await client.PutAsync(EDIT_USERNAME, createRequestContent(this.dataToEdit));
                    break;

                case "password":
                    if (checkPasswordMatches())
                        response = await client.PutAsync(EDIT_PASSWORD, createRequestContent(Password.Password));
                    else
                    {
                        ErrorText.Text = "Passwords doesn't match";
                        ErrorBorder.Visibility = Visibility.Visible;
                        return;
                    }

                    break;

                case "avatar":
                    response = await client.PutAsync(EDIT_AVATAR, createRequestContent(this.dataToEdit));
                    break;

                default: response = null; break;
            }

            string responseString = formatResponse(await response.Content.ReadAsStringAsync());

            if (responseString == "200")
                this.Close();
            else
            {
                ErrorText.Text = responseString;
                ErrorBorder.Visibility = Visibility.Visible;
            }
                
        }

        private StringContent createRequestContent(object data)
        {
            EditRequest request = new EditRequest(data);
            var json = JsonConvert.SerializeObject(request);
            return new StringContent(json, UnicodeEncoding.UTF8, "application/json");
        }

        private void cancel(object sender, RoutedEventArgs e)
        {
            this.Close();
        }

        private string formatResponse(string response)
        {
            char[] MyChar = { '"' };
            response = response.TrimStart(MyChar);
            response = response.TrimEnd(MyChar);
            return response;
        }

        private bool checkPasswordMatches()
        {
            if (Password.Password == ConfirmPassword.Password)
            {
                return true;
            }
            return false;
        }

        private void openDrawingWindow(object sender, RoutedEventArgs e)
        {
            drawWindow = new DrawWindow();
            drawWindow.Show();
            drawWindow.drawingArea.saveBtn.Click += SaveBtn_Click;
        }

        private void SaveBtn_Click(object sender, RoutedEventArgs e)
        {
            this.dataToEdit = drawWindow.drawingArea.getImageData();
            this.drawWindow.Close();
            Avatar.Source = LoadImage((byte[])this.dataToEdit);

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

                this.dataToEdit = new byte[bitmapImage.StreamSource.Length];

                bitmapImage.StreamSource.Seek(0, System.IO.SeekOrigin.Begin); //very important, it should be set to the start of the stream
                bitmapImage.StreamSource.Read((byte[])this.dataToEdit, 0, ((byte[])this.dataToEdit).Length);
                uploadButton.Content = fileDialog.SafeFileName;
                Avatar.Source = LoadImage((byte[])this.dataToEdit);
            }
        }

        private static BitmapImage LoadImage(byte[] imageData)
        {
            if (imageData == null || imageData.Length == 0) return null;
            var image = new BitmapImage();
            using (var mem = new MemoryStream(imageData))
            {
                mem.Position = 0;
                image.BeginInit();
                image.CreateOptions = BitmapCreateOptions.PreservePixelFormat;
                image.CacheOption = BitmapCacheOption.OnLoad;
                image.UriSource = null;
                image.StreamSource = mem;
                image.EndInit();
            }
            image.Freeze();
            return image;
        }
    }
}
