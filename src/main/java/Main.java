import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.EventListener;
import java.util.Scanner;

public class Main extends ListenerAdapter {

    private static JDABuilder builder;
    private static Guild guild;

    public static void main(String[] args) throws FileNotFoundException, LoginException
    {
       File file = new File("tokens.txt");
        Scanner sc = new Scanner(file);
        String line1 = sc.next();
        String line2 = sc.next();
        String discordtoken = line1.substring(16,line1.length()-1);
        System.out.println(discordtoken);
        String openaikey = line2.substring(10,line2.length()-1);
        System.out.println(openaikey);
        OpenAiService service = new OpenAiService(openaikey);
        builder = JDABuilder.createDefault(discordtoken);
        builder.build();

        guild.upsertCommand("chat","starts a new conversation with the openai chatbot!");
       /*ModerationRequest request = new ModerationRequest();
       ModerationResult result = new ModerationResult();*/

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {

    }

}
