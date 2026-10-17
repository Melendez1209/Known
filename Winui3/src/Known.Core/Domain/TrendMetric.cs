namespace Known.Core.Domain;

/// <summary>
/// Which measure the dashboard trend chart plots. Stored in the settings row, so the choice
/// survives a restart.
/// </summary>
public enum TrendMetric
{
    /// <summary>Total marks of each exam. The first run default, and what most students read.</summary>
    Marks = 0,

    /// <summary>Percentage of the full marks each exam was worth.</summary>
    Rate = 1
}