using LunaAvatarStudio.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using System.Diagnostics;
using System.IO;

namespace LunaAvatarStudio.Views;

public sealed partial class SettingsPage : Page
{
    public SettingsPage()
    {
        InitializeComponent();
        DataPathText.Text = ProfileStore.DataDirectory;
    }

    private void OpenDataFolder_Click(object sender, RoutedEventArgs e)
    {
        Directory.CreateDirectory(ProfileStore.DataDirectory);
        Process.Start(new ProcessStartInfo("explorer.exe", ProfileStore.DataDirectory) { UseShellExecute = true });
    }
}
