using System.Runtime.InteropServices;
using Microsoft.UI.Xaml;
using Windows.ApplicationModel.DataTransfer;
using WinRT.Interop;

namespace Known.Services;

/// <summary>
/// Opens the Windows share sheet from a WinUI 3 desktop app.
/// </summary>
/// <remarks>
/// <para>
/// The obvious API, <c>DataTransferManager.GetForCurrentView</c>, is documented as UWP only. On a
/// desktop app there is no CoreWindow to resolve against, and calling it throws. Desktop apps must
/// go through <see cref="IDataTransferManagerInterop"/> and hand it the window handle instead.
/// </para>
/// <para>
/// The handler is registered once when the window is built rather than per click, because the sheet
/// only asks for the payload after the student has picked a target, which is long after the call
/// that opened it has returned.
/// </para>
/// </remarks>
internal static class DesktopShare
{
    /// <summary>IID of <c>IDataTransferManager</c>, as passed to <c>GetForWindow</c>.</summary>
    private static readonly Guid DataTransferManagerIid =
        new(0xa5caee9b, 0x8708, 0x49d1, 0x8d, 0x36, 0x67, 0xd2, 0x5a, 0x8d, 0xa0, 0x0c);

    private static IntPtr _windowHandle;
    private static string _title = string.Empty;
    private static string _text = string.Empty;
    private static bool _ready;

    /// <summary>
    /// Called once from the window constructor. Safe to call more than once; only the first call
    /// registers the handler.
    /// </summary>
    public static void Initialise(Window window)
    {
        ArgumentNullException.ThrowIfNull(window);

        if (_ready)
        {
            return;
        }

        var interop = DataTransferManager.As<IDataTransferManagerInterop>();
        var manager = WinRT.MarshalInterface<DataTransferManager>.FromAbi(
            interop.GetForWindow(WindowNative.GetWindowHandle(window), DataTransferManagerIid));

        manager.DataRequested += OnDataRequested;

        _windowHandle = WindowNative.GetWindowHandle(window);
        _ready = true;
    }

    /// <summary>
    /// Shows the share sheet carrying <paramref name="text"/>.
    /// </summary>
    /// <returns>False when the sheet could not be opened, in which case the text is on the clipboard.</returns>
    public static bool ShareText(string title, string text)
    {
        ArgumentNullException.ThrowIfNull(title);
        ArgumentNullException.ThrowIfNull(text);

        if (!_ready)
        {
            CopyToClipboard(text);
            return false;
        }

        _title = title;
        _text = text;

        try
        {
            DataTransferManager.As<IDataTransferManagerInterop>().ShowShareUIForWindow(_windowHandle);
            return true;
        }
        catch (Exception)
        {
            // Sharing is a convenience, so a failure falls back to the clipboard rather than taking
            // the whole window down
            CopyToClipboard(text);
            return false;
        }
    }

    private static void OnDataRequested(DataTransferManager sender, DataRequestedEventArgs args)
    {
        var data = args.Request.Data;

        data.Properties.Title = _title;
        data.SetText(_text);
    }

    private static void CopyToClipboard(string text)
    {
        try
        {
            var package = new DataPackage { RequestedOperation = DataPackageOperation.Copy };
            package.SetText(text);

            Clipboard.SetContent(package);
            Clipboard.Flush();
        }
        catch (Exception)
        {
            // Nothing left to fall back to
        }
    }
}

/// <summary>
/// Per-window access to the share sheet. Desktop apps have no CoreWindow, so the manager has to be
/// created against an explicit window handle.
/// </summary>
[ComImport]
[Guid("3A3DCD6C-3EAB-43DC-BCDE-45671CE800C8")]
[InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
internal interface IDataTransferManagerInterop
{
    IntPtr GetForWindow([In] IntPtr appWindow, [In] ref Guid riid);

    void ShowShareUIForWindow(IntPtr appWindow);
}
