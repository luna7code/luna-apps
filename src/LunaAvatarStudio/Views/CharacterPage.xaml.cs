using LunaAvatarStudio.Models;
using LunaAvatarStudio.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace LunaAvatarStudio.Views;

public sealed partial class CharacterPage : Page
{
    private readonly ProfileStore _store = new();

    public CharacterPage()
    {
        InitializeComponent();
    }

    private async void Page_Loaded(object sender, RoutedEventArgs e)
    {
        try
        {
            var profile = await _store.LoadAsync();
            CharacterNameBox.Text = profile.Name;
            AvatarTypeBox.SelectedIndex = profile.AvatarType == "3D" ? 1 : 0;
            AvatarAssetPathBox.Text = profile.AvatarAssetPath;
            StatusText.Text = "Local character profile loaded.";
        }
        catch (Exception ex)
        {
            StatusText.Text = $"Could not load profile: {ex.Message}";
        }
    }

    private async void SaveProfile_Click(object sender, RoutedEventArgs e)
    {
        var name = CharacterNameBox.Text.Trim();
        if (string.IsNullOrWhiteSpace(name))
        {
            StatusText.Text = "Enter a character name before saving.";
            return;
        }

        try
        {
            var existing = await _store.LoadAsync();
            var profile = new StudioProfile
            {
                Name = name,
                AvatarType = AvatarTypeBox.SelectedIndex == 1 ? "3D" : "2D",
                AvatarAssetPath = AvatarAssetPathBox.Text.Trim(),
                CreatedUtc = existing.CreatedUtc,
                UpdatedUtc = DateTime.UtcNow
            };
            await _store.SaveAsync(profile);
            StatusText.Text = $"Saved successfully to {_store.ProfilePath}";
        }
        catch (Exception ex)
        {
            StatusText.Text = $"Save failed: {ex.Message}";
        }
    }
}
