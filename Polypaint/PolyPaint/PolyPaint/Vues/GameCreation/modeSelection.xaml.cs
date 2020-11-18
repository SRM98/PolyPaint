using Newtonsoft.Json;
using PolyPaint.Modeles;
using PolyPaint.SocketCom;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
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
    /// Interaction logic for modeSelection.xaml
    /// </summary>
    public partial class modeSelection : Page
    {
        private SocketCommunication socket;
        private DrawingPage myPage;
        private bool init = false;
        private bool clickToContinue = false;

        public modeSelection()
        {
            InitializeComponent();
            DrawingPage.instance.ClearCanvas();
            socket = SocketCommunication.Instance;
            myPage = DrawingPage.instance;
            myPage.DisableDraw();
            myPage.DisableComm();
            DrawingFrame.Content = myPage;
            DrawCom.DrawCommunication.CommunicationEnabled = true;
            myPage.surfaceDessin.setRoom(SocketCommunication.Instance.socketId);
            Game.Instance.difficulty = Difficulty.Easy;
            Game.Instance.mode = DrawingMode.Random;

            socket.subscribe("startPreview", startPreview);
            SocketCommunication.Instance.socket.Emit("preview", JsonConvert.SerializeObject(Game.Instance));

            difficulty.Text = "Easy";
            checkCurrentMode();
            drawingMode.Text = "Random";
            init = true;
        }

        public void startPreview(object svgJson)
        {
            this.Dispatcher.Invoke(() =>
            {
                myPage.ClearCanvas();
            });
        }

        private void checkCurrentMode()
        {
            if (Game.Instance.assistedMode)
            {
                drawingMode.Text = "Replicated";
                drawingMode.Items.Remove(drawingMode.SelectedItem);
            }
        }

        private DrawingMode getDrawingMode(string mode)
        {
            switch (mode)
            {
                case "Random":
                    return DrawingMode.Random;
                case "Panoramic top to bottom":
                    return DrawingMode.PanoramiqueTop;
                case "Panoramic bottom to top":
                    return DrawingMode.PanoramiqueBottom;
                case "Panoramic left to right":
                    return DrawingMode.PanoramiqueLeft;
                case "Panoramic right to left":
                    return DrawingMode.PanoramiqueRight;
                case "Going from center":
                    return DrawingMode.CenteredGoingOut;
                case "Going to center":
                    return DrawingMode.CenteredGoingIn;
                case "Replicated":
                    return DrawingMode.Replicated;
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

        private void submit(object sender, RoutedEventArgs e)
        {
            if (Game.Instance.word.Length == 0)
                MessageBox.Show("No word was set for this game");
            else if (Game.Instance.image == null)
                MessageBox.Show("No image was set for this game");
            else if (Game.Instance.clues.Count == 0)
                MessageBox.Show("No clue was set for this game");
            else
            {
                this.showMessage("The game has been submited!", "(Click create another game)");
                socket.emit("createGame", JsonConvert.SerializeObject(Game.Instance));
            }
        }

        private void DrawingMode_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (init)
            {
                string chosenMode = ((sender as ComboBox).SelectedItem as ComboBoxItem).Content.ToString();
                Game.Instance.mode = getDrawingMode(chosenMode);
                myPage.ClearCanvas();
                SocketCommunication.Instance.socket.Emit("preview", JsonConvert.SerializeObject(Game.Instance));
            }
        }

        private void Difficulty_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (init)
            {
                string chosenDifficutly = ((sender as ComboBox).SelectedItem as ComboBoxItem).Content.ToString();
                Game.Instance.difficulty = getDifficulty(chosenDifficutly);
                myPage.ClearCanvas();
                SocketCommunication.Instance.socket.Emit("preview", JsonConvert.SerializeObject(Game.Instance));
            }
        }

        private void backToSelection(object sender, RoutedEventArgs e)
        {
            DrawCom.DrawCommunication.stopPreview();
            Thread.Sleep(300);
            NavigationService ns = NavigationService.GetNavigationService(this);
            ns.Navigate(new Uri("Vues/GameCreation/cluesPage.xaml", UriKind.Relative));
        }

        public void showMessage(string title, string message = "")
        {
            clickToContinue = true;
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
        private void onCollapseGrid(object sender, RoutedEventArgs e)
        {
            if (clickToContinue)
            {
                Message.Visibility = Visibility.Collapsed;
                clickToContinue = false;
                NavigationService ns = NavigationService.GetNavigationService(this);
                ns.Navigate(new Uri("Vues/GameCreation/GameCreationPage.xaml", UriKind.Relative));
            }
        }
    }
}
