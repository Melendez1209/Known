namespace Known.Core.Domain;

/// <summary>
/// Which measure a dashboard chart plots. Stored in the settings row, so the choice survives a
/// restart. Both the trend chart and the subject chart keep their own value.
/// </summary>
public enum TrendMetric
{
    /// <summary>Total marks of each exam. The first run default, and what most students read.</summary>
    Marks = 0,

    /// <summary>Percentage of the full marks each exam was worth.</summary>
    Rate = 1
}