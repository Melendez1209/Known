using Known.Core.Data;
using Known.Core.Domain;
using Known.Core.Localization;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;
using Windows.System;
using Known.Services;
using Known.Views;

namespace Known.Pages;

/// <summary>
///     The single exam entry form: the exam dates, the exam name and one card per subject holding
///     a tick box, a slider and the two score fields. Saving turns the rows into exam scores
///     through the same domain helpers the Android build used, so the two platforms accept and
///     reject exactly the same input.
/// </summary>
public sealed partial class InputtingPage
{
    private readonly List<ScoreRow> _rows = [];
    private readonly StringLocalizer _strings = new();
    private InputtingNavigation? _navigation;
    private bool _saving;

    /// <summary>Creates the page with a blank form; the real values arrive in OnNavigatedTo.</summary>
    public InputtingPage()
    {
        InitializeComponent();

        SaveButton.Content = _strings.Get("confirm");
        DiscardButton.Content = _strings.Get("discard");
        DatesCaption.Text = _strings.Get("exam_dates");
        HintText.Text = string.Empty;

        // The start of an exam cannot sit after today, so the calendar refuses future dates itself
        // instead of the page rejecting them after the student has picked one
        var today = DateTimeOffset.Now.Date;
        StartPicker.MaxDate = today;
        StartPicker.Date = today;
        EndPicker.Date = today;
    }

    /// <inheritdoc />
    protected override void OnNavigatedTo(NavigationEventArgs e)
    {
        base.OnNavigatedTo(e);

        _navigation = e.Parameter as InputtingNavigation
                      ?? new InputtingNavigation(
                          DateTimeOffset.Now.ToUnixTimeMilliseconds(),
                          DateTimeOffset.Now.ToUnixTimeMilliseconds());

        ApplyNavigationDates();
        Load();
    }

    /// <summary>Shows the exam's own dates, clamping the start onto the no-future rule.</summary>
    private void ApplyNavigationDates()
    {
        if (_navigation is null) return;

        var navigation = _navigation;

        var start = DateTimeOffset.FromUnixTimeMilliseconds(navigation.StartDate);
        var end = DateTimeOffset.FromUnixTimeMilliseconds(navigation.EndDate);

        if (start > StartPicker.MaxDate) start = StartPicker.MaxDate;

        StartPicker.Date = start;
        EndPicker.Date = end;
    }

    /// <summary>
    ///     Builds the form. An existing exam is prefilled, and the subject tick boxes start from the
    ///     selection made during onboarding.
    /// </summary>
    private async void Load()
    {
        if (_navigation is null) return;

        var navigation = _navigation;

        var inputs = ExamScoring.DefaultScoreInputs(AppServices.Settings.SelectedSubjects);
        var name = ExamDates.DefaultExamName(navigation.StartDate);

        if (navigation.ExamId != 0)
        {
            var existing = await AppServices.Exams.Repository.GetExamWithScoresAsync(navigation.ExamId);

            if (existing is not null)
            {
                name = existing.Exam.Name;

                var scores = existing.Scores.ToDictionary(score => score.SubjectKey, StringComparer.Ordinal);

                inputs = inputs
                    .Select(input => scores.TryGetValue(input.SubjectKey, out var score)
                        ? input with
                        {
                            Checked = true,
                            Mark = ExamScoring.FormatScoreValue(score.Mark),
                            FullMark = ExamScoring.FormatScoreValue(score.FullMark)
                        }
                        : input with { Checked = false })
                    .ToList();
            }
        }

        NameButton.Content = string.IsNullOrWhiteSpace(name) ? _strings.Get("name_this") : name;

        _rows.Clear();

        foreach (var input in inputs)
        {
            var row = new ScoreRow(
                input,
                SubjectLabels.NameOf(_strings, input.SubjectKey),
                _strings.Get("mark"),
                _strings.Get("full_mark"));

            // Without this the save button keeps the verdict computed when the form was built, which
            // is "nothing entered", because nothing else told the page that the rows had changed
            row.Changed += (_, _) => UpdateValidation();

            _rows.Add(row);
        }

        SubjectList.ItemsSource = _rows;
        UpdateValidation();
    }

    private async void OnRenameExam(object sender, RoutedEventArgs e)
    {
        var current = NameButton.Content as string ?? string.Empty;

        var box = new TextBox
        {
            Text = current,
            PlaceholderText = _strings.Get("exam_name"),
            AcceptsReturn = false,
            SelectionStart = 0,
            SelectionLength = current.Length
        };

        var dialog = new ContentDialog
        {
            Title = _strings.Get("name_this"),
            Content = box,
            PrimaryButtonText = _strings.Get("done"),
            CloseButtonText = _strings.Get("cancel"),
            // Gives Done the accent treatment and makes it the button the dialogue focuses
            DefaultButton = ContentDialogButton.Primary,
            XamlRoot = XamlRoot
        };

        // Hide takes no result, so the typed text is carried out of the dialogue separately
        string? confirmedByEnter = null;

        // Enter has to behave as the same gesture as pressing Done. The documented behaviour of
        // DefaultButton is that it stops responding to ENTER once focus sits on a control that
        // handles it, which is exactly this text box.
        box.KeyDown += (_, args) =>
        {
            if (confirmedByEnter is not null || args.Key != VirtualKey.Enter || box.AcceptsReturn)
            {
                return;
            }

            confirmedByEnter = box.Text;
            args.Handled = true;

            dialog.Hide();
        };

        var result = await dialog.ShowAsync();

        var name = confirmedByEnter ?? (result == ContentDialogResult.Primary ? box.Text : null);

        if (!string.IsNullOrWhiteSpace(name))
        {
            NameButton.Content = name.Trim();
        }
    }

    /// <summary>
    ///     The domain rules own the verdict, so the message never drifts from what the save will do.
    ///     Every row is re-read here rather than tracked as it is edited, because the slider and the
    ///     text fields both feed the same model. The date pair joins the verdict, so the save stays
    ///     unavailable while the dates contradict each other.
    /// </summary>
    private void UpdateValidation()
    {
        var validation = ExamScoring.ValidateScoreInputs(_rows.Select(row => row.ToScoreInput()));

        ValidationText.Text = validation switch
        {
            ScoreValidation.NoMarkEntered => _strings.Get("no_mark_entered"),
            ScoreValidation.MarkExceedsFull => _strings.Get("mark_exceeds_full"),
            _ => string.Empty
        };

        ValidationText.Visibility = validation == ScoreValidation.Valid
            ? Visibility.Collapsed
            : Visibility.Visible;

        var datesAreValid = StartPicker.Date is { } start
                            && EndPicker.Date is { } end
                            && end >= start
                            && start <= StartPicker.MaxDate;

        HintText.Text = datesAreValid ? string.Empty : _strings.Get("attention");

        SaveButton.IsEnabled = datesAreValid && validation == ScoreValidation.Valid && !_saving;
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

        UpdateValidation();
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

    private async void OnSave(object sender, RoutedEventArgs e)
    {
        if (_saving || _navigation is null) return;

        var navigation = _navigation;

        var inputs = _rows.Select(row => row.ToScoreInput()).ToList();
        var name = (NameButton.Content as string ?? "").Trim();

        var startDate = ToEpochMilliseconds(StartPicker.Date);
        var endDate = ToEpochMilliseconds(EndPicker.Date);

        _saving = true;
        SaveButton.IsEnabled = false;

        try
        {
            if (ExamScoring.BuildExamScores(0, inputs).Count == 0)
            {
                ValidationText.Text = _strings.Get("no_mark_entered");
                ValidationText.Visibility = Visibility.Visible;
                return;
            }

            if (navigation.ExamId == 0)
            {
                var exam = new Exam
                {
                    Name = name,
                    StartDate = startDate,
                    EndDate = endDate
                };

                var scores = ExamScoring.BuildExamScores(0, inputs);

                await AppServices.Exams.MutateAsync(repository => repository.InsertExamAsync(exam, scores));
            }
            else
            {
                var existing = await AppServices.Exams.Repository.GetExamAsync(navigation.ExamId);

                if (existing is not null)
                {
                    existing.Name = name;
                    existing.StartDate = startDate;
                    existing.EndDate = endDate;

                    var scores = ExamScoring.BuildExamScores(existing.Id, inputs);

                    await AppServices.Exams.MutateAsync(repository => repository.UpdateExamAsync(existing, scores));
                }
            }

            this.Frame.GoBack();
        }
        catch (Exception)
        {
            ValidationText.Text = _strings.Get("attention");
            ValidationText.Visibility = Visibility.Visible;
        }
        finally
        {
            _saving = false;
            UpdateValidation();
        }
    }

    private static long ToEpochMilliseconds(DateTimeOffset? value)
    {
        return value?.ToUnixTimeMilliseconds() ?? DateTimeOffset.Now.ToUnixTimeMilliseconds();
    }
}

/// <summary>What the exam entry page needs to open with, passed as the frame navigation parameter.</summary>
/// <param name="StartDate">First day of the exam, as epoch milliseconds.</param>
/// <param name="EndDate">Last day of the exam, as epoch milliseconds.</param>
/// <param name="ExamId">The exam being edited, or zero when a new one is being created.</param>
public sealed record InputtingNavigation(long StartDate, long EndDate, long ExamId = 0);


