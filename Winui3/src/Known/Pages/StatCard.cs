namespace Known.Pages;

/// <summary>
///     One figure in the dashboard summary row. The page owns these directly, so the record only has
///     to carry what the template binds to.
/// </summary>
/// <param name="Value">The figure, already formatted and carrying any unit it needs.</param>
/// <param name="Caption">What the figure means, in the current language.</param>
public sealed record StatCard(string Value, string Caption);