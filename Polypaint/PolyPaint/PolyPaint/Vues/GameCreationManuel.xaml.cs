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
using System.Windows.Shapes;
using Newtonsoft.Json;
using PolyPaint.Convertisseurs;
using PolyPaint.Modeles;
using PolyPaint.SocketCom;

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for GameCreation.xaml
    /// </summary>
    public partial class GameCreationManuel : Page
    {
        private SocketCommunication socket;
        private DrawingPage myPage;
        private List<string> clues;
        public GameCreationManuel()
        {
            InitializeComponent();
            clues = new List<string>();
            DrawingPage.instance.ClearCanvas();
            socket = SocketCommunication.Instance;
            myPage = DrawingPage.instance;
            DrawingFrame.Content = myPage;
            DrawCom.DrawCommunication.CommunicationEnabled = false;
        }

        private void addClue(object sender, RoutedEventArgs e)
        {
            clue.Text.Trim();
            if (clue.Text != "" && clue.Text != null)
                clues.Add(clue.Text);
            else
                MessageBox.Show("Please write a clue before");
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
            StrokeJSON[] svg = myPage.surfaceDessin.CanvasToJSON();
            if (svg.Length == 0)
            {
                MessageBox.Show("You need to draw before creating the game");
            }
            else if (difficulty.SelectedItem == null)
            {
                MessageBox.Show("You need to select a valid difficulty");
            }
            else if (drawingMode.SelectedItem == null)
            {
                MessageBox.Show("You need to select a valid drawing mode");
            }
            else
            {
                Difficulty diff = getDifficulty(difficulty.Text);
                DrawingMode mode = getDrawingMode(drawingMode.Text);
                //Game game = new Game(wordToGuess.Text, svg, mode, diff, clues);
                //socket.emit("createGame", JsonConvert.SerializeObject(game));
            }
        }
    }
}
