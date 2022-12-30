import com.theokanning.openai.OpenAiService;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main extends ListenerAdapter {
    public static String thread;
    private static JDABuilder builder;
    private boolean chatting = false;
    public static OpenAiService service;
    public static void main(String[] args) throws IOException, LoginException, FileNotFoundException
    {
        File file = new File("tokens.txt");
        Scanner sc = new Scanner(file);
        String line1 = sc.next();
        String line2 = sc.next();
        String discordtoken = line1.substring(16);
        System.out.println(discordtoken);
        String openaikey = line2.substring(10);
        System.out.println(openaikey);
        service = new OpenAiService(openaikey);
        builder = JDABuilder.createDefault(discordtoken);
        builder.addEventListeners(new Interaction());
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        builder.build();
        File file1 = new File("prompt.txt");
        Scanner scanner = new Scanner(file1);
        for(int i = 0; i < Files.lines(Path.of("prompt.txt")).count();i++)
        {
            thread += scanner.nextLine() + "\n";
        }

        System.out.println(thread);
    }
    
}
