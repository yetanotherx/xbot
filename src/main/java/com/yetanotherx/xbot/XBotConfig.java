package com.yetanotherx.xbot;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import joptsimple.OptionException;
import java.io.File;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import static com.yetanotherx.xbot.util.Util.asList;

/**
 * XBot Configuration class.
 * 
 * @author yetanotherx
 */
public class XBotConfig {

    private OptionSet options;
    private String username;
    private String password;
    private String url;
    private int editRate;
    private int maxlag;
    private boolean confirmBot;
    private boolean checkTalk;
    private boolean followNoBots;
    private boolean debug;

    private XBotConfig() {
    }

    public static XBotConfig parseArguments(String[] args) {
        OptionSet options = getOptionSet(args);

        Properties props = loadProperties(options);

        XBotConfig out = new XBotConfig();
        out.username = props.getProperty("username");
        out.password = props.getProperty("password");
        out.url = props.getProperty("api", "en.wikipedia.org");
        out.editRate = Integer.parseInt(props.getProperty("editRate", "12"));
        out.maxlag = Integer.parseInt(props.getProperty("maxlag", "5"));
        out.confirmBot = Boolean.parseBoolean(props.getProperty("confirmBot", "true"));
        out.checkTalk = Boolean.parseBoolean(props.getProperty("checkTalk", "false"));
        out.followNoBots = Boolean.parseBoolean(props.getProperty("followNoBots", "true"));
        out.debug = Boolean.parseBoolean(props.getProperty("debug", "false"));

        XBotDebug.debugMode = out.debug;
        
        out.options = options;

        return out;
    }

    private static Properties loadProperties(OptionSet options) {
        Properties props = new Properties();
        InputStream is = null;

        try {
            is = new FileInputStream(new File(options.valueOf("config").toString()));
        } catch (IOException e) {
        }

        try {
            if (is == null) {
                is = XBotMain.class.getResourceAsStream(options.valueOf("config").toString());
            }

            props.load(is);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unknown configuration file specified");
        }
        return props;
    }

    private static OptionSet getOptionSet(String[] args) {
        OptionParser parser = new OptionParser() {

            {
                acceptsAll(asList("?", "help"), "Show the help");

                acceptsAll(asList("c", "config"), "Properties file to use").withRequiredArg().ofType(File.class).defaultsTo(new File("xbot.config")).describedAs("Properties file");

                acceptsAll(asList("l", "enable-logging"), "Whether or not to write logs to a file").withRequiredArg().ofType(Boolean.class).defaultsTo(false).describedAs("Enable log file");

                acceptsAll(asList("o", "log-file"), "Log file to use").withRequiredArg().ofType(File.class).defaultsTo(new File("xbot.log")).describedAs("Log file");

                acceptsAll(asList("nojline"), "Disables jline");

                acceptsAll(asList("noconsole"), "Disables the console");

                acceptsAll(asList("v", "version"), "Show the XBot Version");

            }
        };

        try {
            return parser.parse(args);
        } catch (OptionException ex) {
            Logger.getLogger(XBotMain.class.getName()).log(Level.SEVERE, ex.getMessage());
            return null;
        }
    }

    public OptionSet getOptions() {
        return options;
    }

    public boolean doCheckTalk() {
        return checkTalk;
    }

    public boolean doConfirmBot() {
        return confirmBot;
    }

    public boolean isDebug() {
        return debug;
    }

    public int getEditRate() {
        return editRate;
    }

    public boolean doFollowNoBots() {
        return followNoBots;
    }

    public int getMaxlag() {
        return maxlag;
    }

    public String getPassword() {
        return password;
    }

    public String getURL() {
        return url;
    }

    public String getUsername() {
        return username;
    }
}
