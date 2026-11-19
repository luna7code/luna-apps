using LunaAvatarStudio.Views;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace LunaAvatarStudio;

public sealed partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
        StudioNavigation.SelectedItem = StudioNavigation.MenuItems[0];
        ContentFrame.Navigate(typeof(HomePage));
    }

    private void StudioNavigation_SelectionChanged(NavigationView sender, NavigationViewSelectionChangedEventArgs args)
    {
        if (args.SelectedItem is not NavigationViewItem item)
            return;

        switch (item.Tag?.ToString())
        {
            case "home": ContentFrame.Navigate(typeof(HomePage)); break;
            case "character": ContentFrame.Navigate(typeof(CharacterPage)); break;
            case "studio": ContentFrame.Navigate(typeof(StudioPage)); break;
            case "settings": ContentFrame.Navigate(typeof(SettingsPage)); break;
        }
    }
}
