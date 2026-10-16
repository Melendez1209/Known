using Known.Core.Domain;
using Microsoft.UI.Xaml;

namespace Known.Pages;

/// <summary>
/// One subject's row on the score entry form: a tick box, a slider and the two score fields.
/// </summary>
/// <remarks>
/// This is the only type in the app that is not a page. It has to exist because it backs an
/// <c>ItemsControl</c> item template, where a page property cannot reach. It is a
/// <see cref="DependencyObject"/> rather than a plain notifying class because WinUI binds to
/// dependency properties natively, so the slider and the two text fields stay in step in both
/// directions without any <c>INotifyPropertyChanged</c> machinery.
/// </remarks>
public sealed class ScoreRow : DependencyObject
{
    /// <summary>Guards the slider and the mark field, which write to each other.</summary>
    private bool _syncing;

    /// <summary>Creates a row from the stored input, its translated subject name and field captions.</summary>
    public ScoreRow(ScoreInput source, string subjectName, string markHeader, string fullMarkHeader)
    {
        ArgumentNullException.ThrowIfNull(source);

        SubjectKey = source.SubjectKey;
        SubjectName = subjectName;
        MarkHeader = markHeader;
        FullMarkHeader = fullMarkHeader;

        IsChecked = source.Checked;
        Mark = source.Mark;
        FullMark = source.FullMark;
    }

    /// <summary>Stable subject identifier written to the database.</summary>
    public string SubjectKey { get; }

    /// <summary>Translated subject name shown on the card.</summary>
    public string SubjectName { get; }

    /// <summary>
    /// Captions for the two score fields. They live here rather than in the template because a
    /// <c>DataTemplate</c> cannot carry an x:Name, so the page has no way to reach a named element
    /// inside it to translate it later.
    /// </summary>
    public string MarkHeader { get; }

    /// <summary>Caption for the full mark field.</summary>
    public string FullMarkHeader { get; }

    /// <summary>Whether this row takes part in the saved exam.</summary>
    public bool IsChecked
    {
        get => (bool)GetValue(IsCheckedProperty);
        set => SetValue(IsCheckedProperty, value);
    }

    /// <summary>
    /// The mark as typed. Every assignment runs through the domain pipeline, so a trailing dot
    /// completes to a half point and the field can never hold something the slider or the database
    /// would reject.
    /// </summary>
    public string Mark
    {
        get => (string)GetValue(MarkProperty);
        set => SetValue(MarkProperty, ExamScoring.ProcessScoreInput(value ?? string.Empty));
    }

    /// <summary>The full mark as typed.</summary>
    public string FullMark
    {
        get => (string)GetValue(FullMarkProperty);
        set => SetValue(FullMarkProperty, value ?? string.Empty);
    }

    /// <summary>
    /// Slider position, kept in step with the two text fields in both directions: dragging writes a
    /// snapped mark back into the field, and typing moves the thumb.
    /// </summary>
    public double Slider
    {
        get => (double)GetValue(SliderProperty);
        set => SetValue(SliderProperty, value);
    }

    /// <summary>Projects the row back onto the immutable input the domain layer works with.</summary>
    public ScoreInput ToScoreInput() => new(SubjectKey, IsChecked, Mark, FullMark);

    /// <summary>Backing store for <see cref="IsChecked"/>.</summary>
    public static readonly DependencyProperty IsCheckedProperty =
        DependencyProperty.Register(
            nameof(IsChecked),
            typeof(bool),
            typeof(ScoreRow),
            new PropertyMetadata(false));

    /// <summary>Backing store for <see cref="Mark"/>.</summary>
    public static readonly DependencyProperty MarkProperty =
        DependencyProperty.Register(
            nameof(Mark),
            typeof(string),
            typeof(ScoreRow),
            new PropertyMetadata(string.Empty, OnMarkChanged));

    /// <summary>Backing store for <see cref="FullMark"/>.</summary>
    public static readonly DependencyProperty FullMarkProperty =
        DependencyProperty.Register(
            nameof(FullMark),
            typeof(string),
            typeof(ScoreRow),
            new PropertyMetadata(SubjectCatalogue.DefaultFullMark, OnFullMarkChanged));

    /// <summary>Backing store for <see cref="Slider"/>.</summary>
    public static readonly DependencyProperty SliderProperty =
        DependencyProperty.Register(
            nameof(Slider),
            typeof(double),
            typeof(ScoreRow),
            new PropertyMetadata(0d, OnSliderChanged));

    private static void OnMarkChanged(DependencyObject sender, DependencyPropertyChangedEventArgs args)
    {
        if (sender is not ScoreRow row)
        {
            return;
        }

        row.SyncSliderFromFields();
    }

    private static void OnFullMarkChanged(DependencyObject sender, DependencyPropertyChangedEventArgs args)
    {
        if (sender is not ScoreRow row)
        {
            return;
        }

        row.SyncSliderFromFields();
    }

    private static void OnSliderChanged(DependencyObject sender, DependencyPropertyChangedEventArgs args)
    {
        if (sender is not ScoreRow row || row._syncing)
        {
            return;
        }

        row._syncing = true;

        try
        {
            // Dragging therefore can never leave a value in the field that the form would reject
            row.Mark = ExamScoring.SnappedScore((float)args.NewValue, row.FullMark);
        }
        finally
        {
            row._syncing = false;
        }
    }

    private void SyncSliderFromFields()
    {
        if (_syncing)
        {
            return;
        }

        _syncing = true;

        try
        {
            Slider = ExamScoring.SliderValue(Mark, FullMark);
        }
        finally
        {
            _syncing = false;
        }
    }
}
