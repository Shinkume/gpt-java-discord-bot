import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.moderation.Moderation;
import com.theokanning.openai.moderation.ModerationCategories;
import com.theokanning.openai.moderation.ModerationRequest;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main extends ListenerAdapter {
    private static String thread;
    private static JDABuilder builder;
    private boolean chatting = false;
    private static OpenAiService service;
    public static void main(String[] args) throws IOException, LoginException
    {
       File file = new File("tokens.txt");
        Scanner sc = new Scanner(file);
        String line1 = sc.next();
        String line2 = sc.next();
        String discordtoken = line1.substring(16,line1.length()-1);
        System.out.println(discordtoken);
        String openaikey = line2.substring(10,line2.length()-1);
        System.out.println(openaikey);
        service = new OpenAiService(openaikey);
        builder = JDABuilder.createDefault(discordtoken);
        builder.build();
        File file1 = new File("config.yaml");
        Scanner scanner = new Scanner(file1);
        for(int i = 0; i < Files.lines(Path.of("prompt.txt")).count();i++)
        {
            thread += scanner.next();
        }


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(chatting)
        {
            //reminder to add moderation and openai completion
            chatting = true;
            String message = event.getMessage().getContentRaw();
            thread += event.getAuthor() + "#" + event.getAuthor().getAsTag() + ": " + message + "\n";
            thread += "You: ";


        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().upsertCommand(Commands.slash("chat","Start a conversation with the gpt-3 chatbot!").
                addOption(OptionType.STRING, "message","The message you want to send to the chatbot!")).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("chat"))
        {
            //reminder to add moderation and openai completion

            chatting = true;
            String message = event.getOption("message").getAsString();
            thread += event.getUser() + "#" + event.getUser().getAsTag() + ": " + message + "\n";
            thread += "You: ";
        }
    }

}
