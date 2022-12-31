import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.moderation.Moderation;
import com.theokanning.openai.moderation.ModerationRequest;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;


public class Interaction extends ListenerAdapter {
    private boolean chatting;
    //private int messages;
    private HashMap<String,String> conversations = new HashMap<>();
    private HashMap<String,Integer> messages = new HashMap<>();
    //reminder to add multiple server functionality
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot()) return;
        if(conversations.containsKey(event.getGuild().getId()) && event.getChannel() instanceof ThreadChannel)
        {
            if(!(event.getChannel().asThreadChannel().getName().equals("chat"))) return;
            //reminder to add moderation and openai completion
            String message = event.getMessage().getContentRaw();
            System.out.println(message);
            ModerationRequest request = new ModerationRequest();
            request.setInput(message);
            Moderation moderation = Main.service.createModeration(request).getResults().get(0);
            if(moderation.isFlagged())
            {
                event.getMessage().delete().queue();
                event.getMessage().reply("Your content was flagged by moderation").queue();
                return;
            }else
            {
                if(conversations.get(event.getGuild().getId()).length()+ message.length() >= Main.maxTokens){
                    event.getChannel().sendMessage("I have reached the maximum amount of characters you can send me!").queue();
                    event.getChannel().asThreadChannel().delete().queue();
                    conversations.remove(event.getGuild().getId());
                    return;
                }

                if(message.length() > Main.maxMessageLength)
                {
                    event.getMessage().reply("Your message is too long!").queue();
                    return;
                }
                if(messages.get(event.getGuild().getId()) >= Main.messageLimit)
                {
                    event.getMessage().reply("I have reached the maximum amount of messages you can send me!").queue();
                    event.getChannel().asThreadChannel().delete().queue();
                    conversations.remove(event.getGuild().getId());
                    return;
                }
                messages.replace(event.getGuild().getId(),messages.get(event.getGuild().getId()) + 1);
                conversations.replace(event.getGuild().getId(), conversations.get(event.getGuild().getId()) + event.getAuthor().getName()  + ": " + message + "\n");
                conversations.replace(event.getGuild().getId(), conversations.get(event.getGuild().getId()) + "You: ");
                CompletionRequest completionRequest = new CompletionRequest();
                completionRequest.setPrompt(conversations.get(event.getGuild().getId()));
                completionRequest.setMaxTokens(Main.maxTokens);
                completionRequest.setModel("text-davinci-003");
                completionRequest.setTemperature(0.9);
                String response =  Main.service.createCompletion(completionRequest).getChoices().get(0).getText();
                ModerationRequest request1 = new ModerationRequest();
                request1.setInput(response);
                Moderation moderation1 = Main.service.createModeration(request1).getResults().get(0);
                if(moderation1.isFlagged())
                {
                    event.getChannel().sendMessage("Message was moderated").queue();
                    return;
                }
                event.getChannel().sendMessage(response).queue();
                conversations.replace(event.getGuild().getId(), conversations.get(event.getGuild().getId()) + response + "\n");
                System.out.println(conversations.get(event.getGuild().getId()));
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
        if(event.getUser().isBot()) return;
        if(event.getName().equals("chat"))
        {
            //reminder to add moderation and openai completion

            String message = event.getOption("message").getAsString();
            ModerationRequest request = new ModerationRequest();
            request.setInput(message);
            Moderation moderation = Main.service.createModeration(request).getResults().get(0);
            if(moderation.isFlagged())
            {
                event.reply("Your content was blocked for being offensive").setEphemeral(true).queue();
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
                if(message.length() > Main.maxMessageLength)
                {
                    event.reply("Your message is too long!").setEphemeral(true).queue();
                    return;
                }

                conversations.put(event.getGuild().getId(), Main.prompt);
                messages.put(event.getGuild().getId(), 1);
                event.getChannel().asTextChannel().createThreadChannel("chat").queue();
                conversations.replace(event.getGuild().getId(), conversations.get(event.getGuild().getId()) + event.getUser().getName() +  ": " + message + "\n");
                conversations.replace(event.getGuild().getId(), conversations.get(event.getGuild().getId()) + "You: ");
                CompletionRequest completionRequest = new CompletionRequest();
                completionRequest.setPrompt(conversations.get(event.getGuild().getId()));
                completionRequest.setMaxTokens(Main.maxTokens);
                completionRequest.setModel("text-davinci-003");
                completionRequest.setTemperature(0.9);
                String response =  Main.service.createCompletion(completionRequest).getChoices().get(0).getText();
                conversations.replace(event.getGuild().getId(), conversations.get(event.getGuild().getId()) + response + "\n");
                System.out.println(conversations.get(event.getGuild().getId()));
                ThreadChannel channel = null;
                for(ThreadChannel thread : event.getChannel().asTextChannel().getThreadChannels())
                {
                    if(thread.getName().equals("chat"))
                    {
                        channel = thread;
                    }
                }
                if(channel != null)
                {
                    ModerationRequest request1 = new ModerationRequest();
                    request1.setInput(response);
                    Moderation moderation1 = Main.service.createModeration(request1).getResults().get(0);
                    if(moderation1.isFlagged())
                    {
                        event.getChannel().sendMessage("Message was moderated").queue();
                        return;
                    }
                    channel.sendMessage(response).queue();
                }
            }else
            {
                event.reply("I am already in a thread, sorry!").setEphemeral(true).queue();
                return;
            }

        }
    }

}
