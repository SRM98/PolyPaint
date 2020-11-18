using Newtonsoft.Json;
using PolyPaint.Convertisseurs;
using PolyPaint.Modeles;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
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
    /// Interaction logic for cluesPage.xaml
    /// </summary>
    public partial class cluesPage : Page
    {
        private ObservableCollection<string> cluesToDisplay;
        private DrawingPage myPage;
        public cluesPage()
        {
            InitializeComponent();
            DrawingPage.instance.ClearCanvas();
            myPage = DrawingPage.instance;
            myPage.DisableDraw();
            myPage.DisableComm();
            DrawCom.DrawCommunication.CommunicationEnabled = false;
            DrawingFrame.Width = DrawingFrame.Height;
            DrawingFrame.Content = myPage;
            cluesToDisplay = new ObservableCollection<string>();
            if (Game.Instance.clues == null)
            {
                Game.Instance.clues = new List<string>();
            }
            for (int i = 0; i < Game.Instance.clues.Count; i++)
            {
                cluesToDisplay.Add(Game.Instance.clues[i]);
            }
            cluesList.ItemsSource = cluesToDisplay;
        }

        private void addClue(object sender, RoutedEventArgs e)
        {
            string clue = currentClue.Text.Trim();
            if (cluesToDisplay.Contains(clue))
            {
                ErrorTextBlock.Text = "You already added this clue";
                ErrorTextBlock.Visibility = Visibility.Visible;
            } else if (clue.Length > 0)
            {
                ErrorTextBlock.Visibility = Visibility.Collapsed;
                cluesToDisplay.Add(clue);
                cluesList.ItemsSource = cluesToDisplay;
                currentClue.Text = "";
            } else
            {
                ErrorTextBlock.Text = "Please enter a valid clue";
                ErrorTextBlock.Visibility = Visibility.Visible;
            }
        }

        private void saveClues(object sender, RoutedEventArgs e)
        {
            if (cluesToDisplay.Count == 0)
            {
                ErrorTextBlock.Text = "You need to enter at least one clue";
                ErrorTextBlock.Visibility = Visibility.Visible;
            } else
            {
                ErrorTextBlock.Visibility = Visibility.Collapsed;
                for (int i = 0; i < cluesToDisplay.Count; i++)
                {
                    string clue = cluesToDisplay[i];
                    if(!Game.Instance.clues.Contains(clue))
                        Game.Instance.clues.Add(clue);
                }
                NavigationService ns = NavigationService.GetNavigationService(this);
                ns.Navigate(new Uri("Vues/GameCreation/modeSelection.xaml", UriKind.Relative));
            }
        }

        private void removeClue(object sender, RoutedEventArgs e)
        {
            var button = (Button)sender;
            string clueToRemove = button.CommandParameter.ToString();
            cluesToDisplay.Remove(clueToRemove);
            cluesList.ItemsSource = cluesToDisplay;
        }

        private void back(object sender, RoutedEventArgs e)
        {
            NavigationService ns = NavigationService.GetNavigationService(this);
            if (Game.Instance.assistedMode)
            {
                ns.Navigate(new Uri("Vues/GameCreation/assistedDrawPage.xaml", UriKind.Relative));
            }
            else
            {
                ns.Navigate(new Uri("Vues/GameCreation/drawPageCreation.xaml", UriKind.Relative));
            }
        }

        private void onEnterClue(object sender, KeyEventArgs keyEvent)
        {
            if (keyEvent.Key == Key.Return)
            {
                addClue(null, null);
            }
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            wordToGuess.Text = Game.Instance.word;
            if (Game.Instance.image != null)
            {
                try
                {
                    StrokeJSON[] strokes;
                    if (Game.Instance.assistedMode)
                    {
                        strokes = JsonConvert.DeserializeObject<StrokeJSON[]>(Game.Instance.image.ToString());
                    } else
                    {
                        strokes = Game.Instance.image as StrokeJSON[];
                    }
                    for (int i = 0; i < strokes.Length; ++i)
                        myPage.surfaceDessin.onStartStroke(JsonConvert.SerializeObject(strokes[i]));
                }
                catch { }
            }
        }
    }
}
