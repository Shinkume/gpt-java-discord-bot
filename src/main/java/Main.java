import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.moderation.ModerationRequest;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main extends ListenerAdapter {

    private static JDABuilder builder;
    private boolean chatting = false;
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

       ModerationRequest request = new ModerationRequest();

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().upsertCommand("chat","Allows the user to interact with the gpt-3 chatbot!").queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getCommandString().equals("chat"))
        {

        }
    }

}
