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


public class Interaction extends ListenerAdapter {
    private boolean chatting;
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot()) return;
        System.out.println("a");

        if(chatting && event.getChannel() instanceof ThreadChannel)
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
                if(Main.thread.length() + message.length() >= Main.maxTokens){
                    event.getChannel().sendMessage("I have reached the maximum amount of characters you can send me!").queue();
                    event.getChannel().asThreadChannel().delete();
                    return;
                }

                if(message.length() > Main.maxMessageLength)
                {
                    event.getMessage().reply("Your message is too long!").queue();
                    return;
                }

                Main.thread += event.getAuthor().getName()  + ": " + message + "\n";
                Main.thread += "You: ";
                CompletionRequest completionRequest = new CompletionRequest();
                completionRequest.setPrompt(Main.thread);
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
                Main.thread += response + "\n";
                System.out.println(Main.thread);
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
            chatting = true;
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

                event.getChannel().asTextChannel().createThreadChannel("chat").queue();
                Main.thread += event.getUser().getName() +  ": " + message + "\n";
                Main.thread += "You: ";
                CompletionRequest completionRequest = new CompletionRequest();
                completionRequest.setPrompt(Main.thread);
                completionRequest.setMaxTokens(Main.maxTokens);
                completionRequest.setModel("text-davinci-003");
                completionRequest.setTemperature(0.9);
                String response =  Main.service.createCompletion(completionRequest).getChoices().get(0).getText();
                Main.thread += response + "\n";
                System.out.println(Main.thread);
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
