using System.Text.Json;
using LunaAvatarStudio.Models;

namespace LunaAvatarStudio.Services;

public sealed class ProfileStore
{
    private static readonly JsonSerializerOptions JsonOptions = new() { WriteIndented = true };

    public static string DataDirectory =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "LunaAvatarStudio");

    public static string ProfilePath => Path.Combine(DataDirectory, "character-profile.json");

    public async Task<StudioProfile> LoadAsync(CancellationToken cancellationToken = default)
    {
        if (!File.Exists(ProfilePath))
            return new StudioProfile();

        await using var stream = File.OpenRead(ProfilePath);
        var profile = await JsonSerializer.DeserializeAsync<StudioProfile>(stream, JsonOptions, cancellationToken);
        return profile ?? new StudioProfile();
    }

    public async Task SaveAsync(StudioProfile profile, CancellationToken cancellationToken = default)
    {
        ArgumentNullException.ThrowIfNull(profile);
        Directory.CreateDirectory(DataDirectory);

        var temporaryPath = ProfilePath + ".tmp";
        await using (var stream = new FileStream(temporaryPath, FileMode.Create, FileAccess.Write, FileShare.None, 4096, useAsync: true))
        {
            await JsonSerializer.SerializeAsync(stream, profile, JsonOptions, cancellationToken);
            await stream.FlushAsync(cancellationToken);
        }

        File.Move(temporaryPath, ProfilePath, overwrite: true);
    }
}
