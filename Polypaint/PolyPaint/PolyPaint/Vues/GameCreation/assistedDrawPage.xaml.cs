using FFImageLoading.Svg.Forms;
using Microsoft.Win32;
using Newtonsoft.Json;
using PolyPaint.Modeles;
using PolyPaint.SocketCom;
using PolyPaint.Convertisseurs;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Drawing;
using System.IO;
//using Windows.UI.Xaml.Media;
using System.Linq;
using System.Net;
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
using Svg;
using System.Drawing.Imaging;
using System.Threading;

class ThumbAndUrl
{
    public ThumbAndUrl(ArrayList thumb, ArrayList url)
    {
        thumbList = thumb;
        imageList = url;
    }

    public ArrayList thumbList { get; set; }
    public ArrayList imageList { get; set; }
}

class ThumbDetails
{
    public ThumbDetails(string pThumbUrl, string pImageUrl)
    {
        thumbUrl = pThumbUrl;
        imageUrl = pImageUrl;
    }
    public string thumbUrl { get; set; }
    public string imageUrl { get; set; }

}

namespace PolyPaint.Vues.GameCreation
{
    /// <summary>
    /// Interaction logic for assistedDrawPage.xaml
    /// </summary>
    public partial class assistedDrawPage : Page
    {
        private ArrayList imagesList;
        private ArrayList thumbList;
        private string svgFile;
        private ObservableCollection<ThumbDetails> thumbListToDisplay;
        private SocketCommunication socket;
        private string turn;
        private int turd;
        private int alpha;
        private DrawingPage myPage;
        private int thumbStartIndex = 0;
        private bool clickToContinue = false;
        public assistedDrawPage()
        {
            InitializeComponent();
            myPage = DrawingPage.instance;
            myPage.DisableDraw();
            myPage.ClearCanvas();
            DrawCom.DrawCommunication.CommunicationEnabled = false;
            DrawingFrame.Height = DrawingFrame.Width;
            DrawingFrame.Content = myPage;

            socket = SocketCommunication.Instance;
            socket.subscribeOnce("convertSvgToJson", onJsonResponse);
            socket.subscribeOnce("searchImages", onImagesResponse);
            imagesList = new ArrayList();
            thumbList = new ArrayList();
            thumbListToDisplay = new ObservableCollection<ThumbDetails>();
            turn = "minority";
            turd = 0;
            alpha = 1;
            turnPolicy.Text = turn;
            svgFile = null;
            socket.subscribeOnce("errorConvert", onErrorConvert);
        }

        public void onErrorConvert(object a)
        {
            Dispatcher.Invoke(() =>
            {
                clickToContinue = true;
                progressBar.Visibility = Visibility.Collapsed;
                this.showMessage("Sorry we could not convert the image\n             Please try another one", "(Click to dismiss)");
            });
        }

        private void onCollapseGrid(object sender, RoutedEventArgs e)
        {
            if (clickToContinue)
            {
                Message.Visibility = Visibility.Collapsed;
                progressBar.Visibility = Visibility.Visible;
                clickToContinue = false;
            }
        }

        public void onJsonResponse(object svgJson)
        {
            this.Dispatcher.Invoke(() =>
            {
                Game.Instance = null;
                Game.Instance.word = wordToGuess.Text;
                Game.Instance.image = svgJson;
                myPage.ClearCanvas();
                StrokeJSON[] strokes = JsonConvert.DeserializeObject<StrokeJSON[]>(svgJson.ToString());
                for(int i = 0; i < strokes.Length; ++i)
                    myPage.surfaceDessin.onStartStroke(JsonConvert.SerializeObject(strokes[i]));
                Message.Visibility = Visibility.Collapsed;
            });
        }
        private void showFirstThumbnails()
        {
            this.thumbStartIndex = 0;
            thumbListToDisplay.Clear();
            for (int i = 0; i < 6 && i < thumbList.Count; ++i)
            {
                ThumbDetails thumb = new ThumbDetails((string)thumbList[i], (string)imagesList[i]);
                thumbListToDisplay.Add(thumb);
            }
            imageListControl.ItemsSource = thumbListToDisplay;
        }

        public void onImagesResponse(object data /*Task<HttpResponse<string>> res*/)
        {
            
            this.Dispatcher.Invoke(() =>
            {
                ThumbAndUrl lists = JsonConvert.DeserializeObject<ThumbAndUrl>(data.ToString());
                thumbList = lists.thumbList;
                Console.WriteLine(thumbList);
                imagesList = lists.imageList;
                showFirstThumbnails();
                if (thumbList.Count > 6)
                {
                    previousButton.Visibility = Visibility.Hidden;
                    nextButton.Visibility = Visibility.Visible;
                }
                else
                {
                    previousButton.Visibility = Visibility.Hidden;
                    nextButton.Visibility = Visibility.Hidden;
                }
                Message.Visibility = Visibility.Collapsed;
            });
        }

        private void executePotrace()
        {
            ProcessStartInfo potrace = new ProcessStartInfo();
            potrace.CreateNoWindow = true;
            potrace.WindowStyle = ProcessWindowStyle.Hidden;
            potrace.FileName = "potrace.exe";
            potrace.Arguments = "--svg -i bmpin.bmp -o svgout.svg -z " + turn + " -t " + (turd + 10) + " -a " + alpha/100
                ;
            try
            {
                string path = Directory.GetCurrentDirectory() + @"\svgout.svg";
                using (Process exePotrace = Process.Start(potrace))
                {
                    exePotrace.WaitForExit();

                    WebClient localClient = new WebClient();
                    svgFile = localClient.DownloadString(path);
                }
                socket.emit("convertSvgToJson", svgFile);
            }
            catch
            {
                MessageBox.Show("We are sorry we couldn't convert the image to svg. Please try another image");
            }
        }

        private void searchImages(object sender, RoutedEventArgs e)
        {
            this.showMessage("Searching images...");
            socket.emit("searchImages", wordToGuess.Text);
        }

        private void downloadImageThread(string parameter)
        {
            WebClient downloader = new WebClient();
            Stream stream;
            try
            {
                stream = downloader.OpenRead(parameter);
                Bitmap bitmap; bitmap = new Bitmap(stream);

                if (bitmap == null)
                {
                    Dispatcher.Invoke(() => {
                        MessageBox.Show("The image could not be downloaded!");
                        Message.Visibility = Visibility.Collapsed;
                    });
                }
                else
                {
                    bitmap.Save(Directory.GetCurrentDirectory() + @"\bmpin.bmp", ImageFormat.Bmp);
                }

                stream.Flush();
                stream.Close();
                downloader.Dispose();
                Thread thr = new Thread(new ThreadStart(executePotrace));
                thr.Start();
            }
            catch (Exception)
            {
                Dispatcher.Invoke(() => {
                    MessageBox.Show("Could not download image");
                    Message.Visibility = Visibility.Collapsed;
                });
            }
        }

        private void downloadImage(object sender, RoutedEventArgs e)
        {
            var button = (Button)sender;
            string param = button.CommandParameter.ToString();
            this.showMessage("Converting image ...");
            Thread thr = new Thread(() => downloadImageThread(param));
            thr.Start();
        }
        private void uploadImage(object sender, RoutedEventArgs e)
        {
            OpenFileDialog op = new OpenFileDialog();
            op.Title = "Select a picture";
            op.Filter = "All supported graphics|*.jpg;*.jpeg;*.png;*.bmp|" +
              "JPEG (*.jpg;*.jpeg)|*.jpg;*.jpeg|" +
              "Portable Network Graphic (*.png)|*.png|" +
              "BMP (*.bmp)|*.bmp";
            if (op.ShowDialog() == true)
            {
                Bitmap bitmap = new Bitmap(op.FileName);
                bitmap.Save(Directory.GetCurrentDirectory() + @"\bmpin.bmp", ImageFormat.Bmp);
                executePotrace();
            }
        }
        private void saveDrawing(object sender, RoutedEventArgs e)
        {
            if (Game.Instance.image == null)
            {
                MessageBox.Show("You need to add a valid image before creating the game");
            } else
            {
                Game.Instance.assistedMode = true;
                NavigationService ns = NavigationService.GetNavigationService(this);
                ns.Navigate(new Uri("Vues/GameCreation/cluesPage.xaml", UriKind.Relative));
            }
        }

        private void UpdatePotrace(object sender, System.Windows.Controls.Primitives.DragCompletedEventArgs e)
        {
            turd = (int)turdSize.Value;
            alpha = (int)alphaMax.Value;
            executePotrace();
        }

        public void showMessage(string title, string message = "")
        {
            MessageTitle.Text = title;
            MessageTitle.Visibility = Visibility.Visible;
            if (message != "")
            {
                MessageText.Text = message;
                MessageText.Visibility = Visibility.Visible;
            }
            else
            {
                MessageText.Visibility = Visibility.Collapsed;
            }
            Message.Visibility = Visibility.Visible;
        }

        private void TurnPolicy_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            turn = turnPolicy.Text;
            if (svgFile != null)
                executePotrace();
        }

        private void NextImagesButton_Click(object sender, RoutedEventArgs e)
        {
            previousButton.Visibility = Visibility.Visible;
            if (this.thumbStartIndex + 12 >= thumbList.Count)
            {
                nextButton.Visibility = Visibility.Hidden;
            }
            else
            {
                nextButton.Visibility = Visibility.Visible;
            }
            this.thumbStartIndex = this.thumbStartIndex + 6;
            thumbListToDisplay.Clear();
            for (int i = this.thumbStartIndex; i < this.thumbStartIndex + 6 && i < thumbList.Count; ++i)
            {
                ThumbDetails thumb = new ThumbDetails((string)thumbList[i], (string)imagesList[i]);
                thumbListToDisplay.Add(thumb);
            }
            imageListControl.ItemsSource = thumbListToDisplay;

        }

        private void PreviousImagesButton_Click(object sender, RoutedEventArgs e)
        {
            nextButton.Visibility = Visibility.Visible;
            if (this.thumbStartIndex - 12 < 0)
            {
                this.thumbStartIndex = 0;
                previousButton.Visibility = Visibility.Hidden;
            }
            else
            {
                previousButton.Visibility = Visibility.Visible;
            }
            this.thumbStartIndex = this.thumbStartIndex - 6;
            if (this.thumbStartIndex < 0) this.thumbStartIndex = 0;
            thumbListToDisplay.Clear();
            for (int i = this.thumbStartIndex; i < this.thumbStartIndex + 6 && i < thumbList.Count; ++i)
            {
                ThumbDetails thumb = new ThumbDetails((string)thumbList[i], (string)imagesList[i]);
                thumbListToDisplay.Add(thumb);
            }
            imageListControl.ItemsSource = thumbListToDisplay;
        }

        private void backToSelection(object sender, RoutedEventArgs e)
        {
            NavigationService ns = NavigationService.GetNavigationService(this);
            ns.Navigate(new Uri("Vues/GameCreation/GameCreationPage.xaml", UriKind.Relative));
        }

        private void onEnterSearchImage(object sender, KeyEventArgs keyEvent)
        {
            if (keyEvent.Key == Key.Return)
            {
                searchImages(null, null);
            }
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            wordToGuess.Text = Game.Instance.word;
            if (Game.Instance.image != null)
            {
                StrokeJSON[] strokes = JsonConvert.DeserializeObject<StrokeJSON[]>(Game.Instance.image.ToString());
                for (int i = 0; i < strokes.Length; ++i)
                    myPage.surfaceDessin.onStartStroke(JsonConvert.SerializeObject(strokes[i]));
            }
        }
    }
}
