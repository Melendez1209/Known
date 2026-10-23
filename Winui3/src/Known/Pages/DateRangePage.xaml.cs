using Known.Core.Localization;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Known.Pages;

/// <summary>
///     The first step of recording an exam: when it happened.
/// </summary>
/// <remarks>
///     The Android build used the Material date range picker, which WinUI has no equivalent of. This
///     page therefore offers two calendar pickers with an explicit rule that the end cannot precede the
///     start, which is the behaviour that actually matters to the student.
/// </remarks>
public sealed partial class DateRangePage : Page
{
    private readonly StringLocalizer _strings = new();

    /// <summary>Creates the page with today pre-selected on both ends.</summary>
    public DateRangePage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("exam_dates");
        StartPicker.Header = _strings.Get("exam_dates");
        EndPicker.Header = _strings.Get("exam_dates");
        DiscardButton.Content = _strings.Get("discard");
        NextButton.Content = _strings.Get("next");
        HintText.Text = string.Empty;

        var today = DateTimeOffset.Now;
        StartPicker.Date = today;
        EndPicker.Date = today;

        UpdateValidity();
    }

    private void OnDateChanged(CalendarDatePicker sender, CalendarDatePickerDateChangedEventArgs args)
    {
        // Nudging the other picker keeps the pair valid as it is filled in, rather than disabling a
        // button and leaving the student to work out why it went grey
        if (sender == StartPicker)
        {
            if (StartPicker.Date is { } start && EndPicker.Date is { } end && end < start) EndPicker.Date = start;
        }
        else if (EndPicker.Date is { } chosen && StartPicker.Date is { } from && chosen < from)
        {
            StartPicker.Date = chosen;
        }

        UpdateValidity();
    }

    private void UpdateValidity()
    {
        var start = StartPicker.Date;
        var end = EndPicker.Date;

        var isValid = start is not null && end is not null && end >= start;

        NextButton.IsEnabled = isValid;
        HintText.Text = isValid ? string.Empty : _strings.Get("attention");
    }

    private async void OnDiscard(object sender, RoutedEventArgs e)
    {
        var dialog = new ContentDialog
        {
            Title = _strings.Get("attention"),
            Content = _strings.Get("discard_exam_confirm"),
            PrimaryButtonText = _strings.Get("discard"),
            CloseButtonText = _strings.Get("cancel"),
            XamlRoot = XamlRoot
        };

        if (await dialog.ShowAsync() == ContentDialogResult.Primary) this.Frame.GoBack();
    }

    private void OnNext(object sender, RoutedEventArgs e)
    {
        var start = ToEpochMilliseconds(StartPicker.Date);
        var end = ToEpochMilliseconds(EndPicker.Date);

        this.Frame.Navigate(typeof(InputtingPage), new InputtingNavigation(start, end));
    }

    private static long ToEpochMilliseconds(DateTimeOffset? value)
    {
        return value?.ToUnixTimeMilliseconds() ?? DateTimeOffset.Now.ToUnixTimeMilliseconds();
    }
}

/// <summary>What the score entry page needs to open with, passed as the frame navigation parameter.</summary>
/// <param name="StartDate">First day of the exam, as epoch milliseconds.</param>
/// <param name="EndDate">Last day of the exam, as epoch milliseconds.</param>
/// <param name="ExamId">The exam being edited, or zero when a new one is being created.</param>
public sealed record InputtingNavigation(long StartDate, long EndDate, long ExamId = 0);

