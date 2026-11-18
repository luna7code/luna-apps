namespace LunaAvatarStudio.Models;

public sealed class StudioProfile
{
    public string Name { get; set; } = "My Avatar";
    public string AvatarType { get; set; } = "2D";
    public string AvatarAssetPath { get; set; } = string.Empty;
    public DateTime CreatedUtc { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedUtc { get; set; } = DateTime.UtcNow;
}
