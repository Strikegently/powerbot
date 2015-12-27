import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.rt6.ClientContext;

@Script.Manifest(name = "Hello, RSBot!", description = "A 'Hello, World' example for RSBot")
public class HelloRSBot extends PollingScript<ClientContext> {
    @Override
    public void poll() {
        System.out.println("Hello, RSBot!");
    }
}
