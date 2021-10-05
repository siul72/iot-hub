package co.luism.lukisoftiot;

import co.luism.lukisoftiot.common.DiagnosticsConfig;
import co.luism.lukisoftiot.common.Utils;
import co.luism.lukisoftiot.webapputils.WebAppUtils;
import picocli.CommandLine;

@CommandLine.Command(
        name = "lukiiot", mixinStandardHelpOptions = true, version = "0.0.1"
)
public class LukiiotCommand implements Runnable {

    @CommandLine.Command(name = "db-create")
    void CreateDatabase(@CommandLine.Option(names = {"-u", "--user-name"}, required = true,
            description = "database user name") String user_name,
            @CommandLine.Option(names = {"-p", "--user-password"}, required = true,
            description = "database user password") String user_password){
        Utils.createSchema(user_name, user_password);
    }

    @CommandLine.Command(name = "run")
    void RunApplication() throws InterruptedException {

        boolean runLoop = true;
        WebAppUtils myDCM = WebAppUtils.getInstance();
        myDCM.init(WebAppUtils.class);

        while (runLoop) {
            Thread.sleep(1000);
        }

    }

    @CommandLine.Command(name = "db-test")
    void TestDb()  {

        boolean test = Utils.testDatabase();

        if (test) {
            System.out.println("Test ok");
        } else {
            System.out.println("Test failed");
        }

    }

    @Override
    public void run() {
        System.out.println("The popular git command");
    }
}


