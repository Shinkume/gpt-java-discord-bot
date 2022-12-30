import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.moderation.Moderation;
import com.theokanning.openai.moderation.ModerationRequest;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

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
        String discordtoken = line1.substring(16,line1.length()-1);
        System.out.println(discordtoken);
        String openaikey = line2.substring(10,line2.length()-1);
        System.out.println(openaikey);
        service = new OpenAiService(openaikey);
        builder = JDABuilder.createDefault(discordtoken);
        builder.build();
        builder.addEventListeners(new Interaction());
        File file1 = new File("prompt.txt");
        Scanner scanner = new Scanner(file1);
        for(int i = 0; i < Files.lines(Path.of("prompt.txt")).count();i++)
        {
            thread += scanner.next();
        }

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(chatting && event.getChannel() instanceof  ThreadChannel)
        {
            if(!(event.getChannel().asThreadChannel().getName().equals("chat"))) return;
            //reminder to add moderation and openai completion
            String message = event.getMessage().getContentRaw();
            ModerationRequest request = new ModerationRequest();
            request.setInput(message);
            Moderation moderation = service.createModeration(request).getResults().get(0);
            if(moderation.isFlagged())
            {
                event.getMessage().delete().queue();
                event.getMessage().reply("Your content was flagged by moderation").queue();
                return;
            }else
            {
                thread += event.getAuthor() + "#" + event.getAuthor().getAsTag() + ": " + message + "\n";
                thread += "You: ";
                CompletionRequest completionRequest = new CompletionRequest();
                completionRequest.setPrompt(thread);
                completionRequest.setMaxTokens(250);
                completionRequest.setModel("davinci-003");
                completionRequest.setTemperature(0.9);
                String response =  service.createCompletion(completionRequest).getChoices().get(0).getText();
                event.getChannel().sendMessage(response).queue();
            }

        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().upsertCommand(Commands.slash("chat","Start a conversation with the gpt-3 chatbot!").
                addOption(OptionType.STRING, "message","The message you want to send to the chatbot!")).queue();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
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
            ModerationRequest request = new ModerationRequest();
            request.setInput(message);
            Moderation moderation = service.createModeration(request).getResults().get(0);
                if(moderation.isFlagged())
                  {
                      event.reply("Your content was blocked for being offensive").setEphemeral(true);
                      return;
                  }else if(!(event.getChannel() instanceof  ThreadChannel ))
                  {
                      for(ThreadChannel thread : event.getChannel().asTextChannel().getThreadChannels())
                      {
                          if(thread.getName().equals("chat"))
                          {
                              event.reply("I am already in a thread, sorry!").setEphemeral(true).queue();
                              return;
                          }
                      }
                      event.getChannel().asTextChannel().createThreadChannel("chat").queue();
                      thread += event.getUser() + "#" + event.getUser().getAsTag() + ": " + message + "\n";
                      thread += "You: ";
                      CompletionRequest completionRequest = new CompletionRequest();
                      completionRequest.setPrompt(thread);
                      completionRequest.setMaxTokens(250);
                      completionRequest.setModel("davinci-003");
                      completionRequest.setTemperature(0.9);
                      String response =  service.createCompletion(completionRequest).getChoices().get(0).getText();
                      ThreadChannel channel = null;
                      for(ThreadChannel thread : event.getChannel().asTextChannel().getThreadChannels())
                      {
                          if(thread.getName().equals("chat"))
                          {
                             channel = thread;
                          }
                      }
                     if(channel != null) channel.sendMessage(response).queue();
                  }else
                  {
                      event.reply("I am already in a thread, sorry!").setEphemeral(true).queue();
                      return;
                  }

        }
    }

}
