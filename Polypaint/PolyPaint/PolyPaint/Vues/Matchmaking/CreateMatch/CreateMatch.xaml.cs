using System;
using System.Collections.Generic;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using Newtonsoft.Json;
using PolyPaint.ChatComm;
using PolyPaint.Modeles;
using System.Windows.Media;
using System.Collections.ObjectModel;
using PolyPaint.SocketCom;
using System.Linq;

//struct MessageInfo
//{
//    public string room;
//    public string Text;
//    public string sender;
//    public long time;
//}

namespace PolyPaint.Vues
{
    /// <summary>
    /// Interaction logic for Window1.xaml
    /// </summary>
    public partial class CreateMatch : Page
    {
        private Action LeaveFunction;
        private Action<IModeInfo> CreateFunction;
        public CreateMatch()
        {
            InitializeComponent();
        }
        public void setLeaveFunction(Action action)
        {
            this.LeaveFunction = action;
        }

        public void setCreateFunction(Action<IModeInfo> action)
        {
            this.CreateFunction = action;
        }

        private void LeaveButton(object sender, RoutedEventArgs e)
        {
            this.LeaveFunction();
        }

        private void CreateButton(object sender, RoutedEventArgs e)
        {
            RadioButton radioButton = null;
            radioButton = GetCheckedRadioButton(ModeRadioButtons.Children, "Modes");

            IModeInfo newGame = new IModeInfo();
            newGame.creator = Account.Instance.username;


            if (name.Text != "")
            {
                newGame.name = name.Text;
            }
            else
            {
                ErrorTextBlock.Text = "Please choose a game name";
                ErrorTextBlock.Visibility = Visibility.Visible;
                return;
            }

            if (radioButton != null)
            {
                Console.WriteLine(radioButton.Name);
                switch (radioButton.Name) {
                    case "Classic": newGame.type = Modes.Classic; break;
                    case "Coop":    newGame.type = Modes.Coop; break;
                    case "Duel":    newGame.type = Modes.Duel; break;
                }
            } else {
                ErrorTextBlock.Text = "Please choose a game mode";
                ErrorTextBlock.Visibility = Visibility.Visible;
                return;
            }      

            newGame.nbRounds = (int)nRounds.Value;

            this.CreateFunction(newGame);

        }
        private RadioButton GetCheckedRadioButton(UIElementCollection children, String groupName)
        {
            return children.OfType<RadioButton>().FirstOrDefault(rb => rb.IsChecked == true && rb.GroupName == groupName);
        }

        private void onRadioButtonClick(object sender, RoutedEventArgs e)
        {
            string categorie = ((RadioButton)sender).Tag.ToString();
            switch(categorie)
            {
                case "Classic": RoundChooser.Visibility = Visibility.Visible; break;
                case "Coop": RoundChooser.Visibility = Visibility.Collapsed; break;
                case "Duel": RoundChooser.Visibility = Visibility.Visible;  break;

            }
        }

    }
}
