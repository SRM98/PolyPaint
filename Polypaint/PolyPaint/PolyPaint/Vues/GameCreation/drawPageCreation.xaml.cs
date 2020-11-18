using Newtonsoft.Json;
using PolyPaint.Convertisseurs;
using PolyPaint.Modeles;
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

namespace PolyPaint.Vues.GameCreation
{
    /// <summary>
    /// Interaction logic for drawPageCreation.xaml
    /// </summary>
    public partial class drawPageCreation : Page
    {
        private DrawingPage myPage;
        private bool isInitializing;
        public drawPageCreation()
        {
            InitializeComponent();
            DrawingPage.instance.ClearCanvas();
            myPage = DrawingPage.instance;
            myPage.EnableDraw();
            myPage.DisableComm();
            DrawingFrame.Content = myPage;
            DrawCom.DrawCommunication.CommunicationEnabled = false;
            isInitializing = true;
            wordToGuess.Text = Game.Instance.word;
        }

        private void saveDrawingManuel(object sender, RoutedEventArgs e)
        {
            Game.Instance.assistedMode = false;
            string word = wordToGuess.Text.Trim();
            StrokeJSON[] svg = myPage.surfaceDessin.CanvasToJSON();
            if (word == "")
            {
                ErrorTextBlock.Text = "Please enter a word associated with the drawing";
                ErrorTextBlock.Visibility = Visibility.Visible;
            } else if (svg.Length == 0)
            {
                ErrorTextBlock.Text = "You need to draw before creating the game";
                ErrorTextBlock.Visibility = Visibility.Visible;
            } else
            {
                ErrorTextBlock.Visibility = Visibility.Collapsed;
                Game.Instance.word = word;
                Game.Instance.image = svg;
                NavigationService ns = NavigationService.GetNavigationService(this);
                ns.Navigate(new Uri("Vues/GameCreation/cluesPage.xaml", UriKind.Relative));
            }
        }

        private void backToSelectionManuel(object sender, RoutedEventArgs e)
        {
            NavigationService ns = NavigationService.GetNavigationService(this);
            ns.Navigate(new Uri("Vues/GameCreation/GameCreationPage.xaml", UriKind.Relative));
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            try
            {
                if (Game.Instance.image != null)
                {
                    StrokeJSON[] strokes = Game.Instance.image as StrokeJSON[];
                    for (int i = 0; i < strokes.Length; ++i)
                        myPage.surfaceDessin.onStartStroke(JsonConvert.SerializeObject(strokes[i]));
                }
            }
            catch { }
        }

        private void WordToGuess_SelectionChanged(object sender, RoutedEventArgs e)
        {
            if(!isInitializing)
                Game.Instance = null;
            isInitializing = false;
        }
    }
}
