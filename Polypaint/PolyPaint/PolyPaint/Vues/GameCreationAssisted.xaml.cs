using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using unirest_net.http;
using Newtonsoft.Json;
using System.Collections;
using System.Text.RegularExpressions;
using System.Collections.ObjectModel;
using System.Net;
using System.IO;
using System.Drawing;
using System.Drawing.Imaging;
using System.Diagnostics;
using PolyPaint.Modeles;
using PolyPaint.SocketCom;
using Microsoft.Win32;


namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Window2.xaml
    /// </summary>
    public partial class GameCreationAssisted : Page
    {
        private ArrayList imagesList;
        private ArrayList thumbList;
        private List<string> clues;
        private string svgFile;
        private ObservableCollection<ThumbDetails> thumbListToDisplay;
        private SocketCommunication socket;
        public GameCreationAssisted()
        {
            InitializeComponent();
            socket = SocketCommunication.Instance;
            socket.subscribe("convertSvgToJson", onJsonResponse);
            socket.subscribe("searchImages", onImagesResponse);
            imagesList = new ArrayList();
            thumbList = new ArrayList();
            clues = new List<string>();
            thumbListToDisplay = new ObservableCollection<ThumbDetails>();
        }

        public void onJsonResponse(object svgJson)
        {
            this.Dispatcher.Invoke(() =>
            {
                Difficulty diff = getDifficulty(difficulty.Text);
                DrawingMode mode = getDrawingMode(drawingMode.Text);
                //Game game = new Game(wordToGuess.Text, svgJson, mode, diff, clues);
                //Console.WriteLine(JsonConvert.SerializeObject(game));
                //socket.emit("createGame", JsonConvert.SerializeObject(game));
            });
        }

        private void showFirstThumbnails()
        {
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
                Console.WriteLine(data);
                ThumbAndUrl lists = JsonConvert.DeserializeObject<ThumbAndUrl>(data.ToString());
                thumbList = lists.thumbList;
                imagesList = lists.imageList;
                showFirstThumbnails();
            });
        }

        private void searchImages(object sender, RoutedEventArgs e)
        {
            socket.emit("searchImages", wordToGuess.Text);
        }

        private void executePotrace()
        {
            ProcessStartInfo potrace = new ProcessStartInfo();
            potrace.CreateNoWindow = true;
            potrace.WindowStyle = ProcessWindowStyle.Hidden;
            potrace.FileName = "potrace.exe";
            potrace.Arguments = "--svg -i bmpin.bmp -o svgout.svg";

            try
            {
                using (Process exePotrace = Process.Start(potrace))
                {
                    exePotrace.WaitForExit();

                    System.Net.WebClient localClient = new System.Net.WebClient();
                    svgFile = localClient.DownloadString(Directory.GetCurrentDirectory() + @"\svgout.svg");
                }
            }
            catch
            {
                MessageBox.Show("We are sorry we couldn't convert the image to svg. Please try another image");
            }
        }

        private void downloadImage(object sender, RoutedEventArgs e)
        {
            var button = (Button)sender;
            WebClient downloader = new WebClient();
            Stream stream;
            try
            {
                stream = downloader.OpenRead(button.CommandParameter.ToString());
                Bitmap bitmap; bitmap = new Bitmap(stream);

                if(bitmap == null)
                {
                    MessageBox.Show("The image could not be downloaded!");
                } else
                {
                    bitmap.Save(Directory.GetCurrentDirectory() + @"\bmpin.bmp", ImageFormat.Bmp);
                }

                stream.Flush();
                stream.Close();
                downloader.Dispose();
                executePotrace();
            } catch (Exception)
            {
                MessageBox.Show("Could not download image");
            }
        }

        private void submit(object sender, RoutedEventArgs e)
        {
            if (svgFile == null)
            {
                MessageBox.Show("You need to add a valid image before creating the game");
            } else if (difficulty.SelectedItem == null)
            {
                MessageBox.Show("You need to select a valid difficulty");
            }
            else if (drawingMode.SelectedItem == null)
            {
                MessageBox.Show("You need to select a valid drawing mode");
            } else if (clues.Count == 0) {
                MessageBox.Show("You need to enter at least one clue");
            } else
            {
                socket.emit("convertSvgToJson", svgFile);
            }
        }

        private DrawingMode getDrawingMode(string mode)
        {
            switch (mode)
            {
                case "Random":
                    return DrawingMode.Random;
                case "Panoramique top to bottom":
                    return DrawingMode.PanoramiqueTop;
                case "Panoramique bottom to top":
                    return DrawingMode.PanoramiqueBottom;
                case "Panoramique left to right":
                    return DrawingMode.PanoramiqueLeft;
                case "Panoramique right to left":
                    return DrawingMode.PanoramiqueRight;
                case "Going from center":
                    return DrawingMode.CenteredGoingOut;
                case "Going to center":
                    return DrawingMode.CenteredGoingIn;
                default:
                    throw new Exception("Drawing mode is not working");
            }
        }

        private Difficulty getDifficulty(string diff)
        {
            switch (diff)
            {
                case "Easy":
                    return Difficulty.Easy;
                case "Intermediate":
                    return Difficulty.Intermediate;
                case "Difficult":
                    return Difficulty.Hard;
                default:
                    throw new Exception("Difficulty is not working");
            }
        }

        private void addClue(object sender, RoutedEventArgs e)
        {
            clue.Text.Trim();
            if (clue.Text != "" && clue.Text != null)
                clues.Add(clue.Text);
            else
                MessageBox.Show("Please write a clue before");
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
    }
}
