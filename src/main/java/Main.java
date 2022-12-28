import com.theokanning.openai.OpenAiService;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws FileNotFoundException
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
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(discordtoken);
    }

}
