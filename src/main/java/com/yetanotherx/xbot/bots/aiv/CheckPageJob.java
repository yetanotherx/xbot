package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.bots.BotJob;
import java.util.HashMap;
import java.util.Map;

public class CheckPageJob extends BotJob {

    private String page;

    public CheckPageJob(String page, BotThread bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
        this.page = page;
    }

    @Override
    public void doRun() {
        /*XBotDebug.debug("AIV", "Getting page " + page + "...");
        AIVBot aivbot = (AIVBot) bot;

        String content = aivbot.getParent().getWiki().readData(page).getText();
        if (!content.isEmpty()) {
            Matcher m = Pattern.compile("\\{\\{((?:no)?adminbacklog)\\}\\}\\s*<\\!-- (?:HBC AIV helperbot )?v([\\d.]+) ((?:\\w+=\\S+\\s+)+)-->", Pattern.CASE_INSENSITIVE).matcher(content);

            if (m.find()) {

                boolean backlog = m.group(1).equals("adminbacklog");
                String version = m.group(2);
                String parameter_str = m.group(3);

                if (!version.equals(aivbot.getVersion())) {
                    XBotDebug.warn("AIV", "Version is out of date! Required version: " + version);
                    return;
                }

                Map<String, String> params = this.parseParams(parameter_str);

                if (params.get("FixInstructions").equals("on")) {
                    if (!this.fixInstructions()) {
                        return;
                    }
                }

                int reportCount = 0;
                String inComment = "";
                Map<String, Integer> userCount = new HashMap<String, Integer>();
                boolean merged = false;
                List<List<String>> ipCommentsNeeded = new ArrayList<List<String>>();

                for (String line : content.split("\n")) {
                    String[] comment = this.parseComment(line, inComment);
                    inComment = comment[0];
                    String bareLine = comment[1];

                    m = Pattern.compile("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(.+?)\\s*\\}\\}", Pattern.CASE_INSENSITIVE).matcher(bareLine);
                    if (!m.find()) {
                        continue;
                    }

                    String user = m.group(2);
                    if (user.split("=").length > 1) {
                        user = user.split("=")[1];
                    }

                    reportCount++;
                    if (userCount.containsKey(user)) {
                        userCount.put(user, userCount.get(user) + 1);
                    } else {
                        userCount.put(user, 1);
                    }

                    if (userCount.get(user) > 1 && !merged && params.get("MergeDuplicates").equals("on")) {
                        XBotDebug.info("AIV", "Calling merge for " + user + " on " + page);
                        this.bot.addJob(new MergeDuplicatesJob(page, aivbot, 0, false));
                        merged = true;
                    }

                    if (params.get("RemoveBlocked").equals("on")) {
                        this.bot.addJob(new CheckUserJob(user, aivbot, 0, false));
                    }

                    List<String> cats = this.checkUserInCats(user, aivbot.getCategories());
                    if (!cats.isEmpty()) {
                        String message = "User is in the ";
                        message += (cats.size() > 1 ? "categories" : "category") + ": ";

                        for (int i = 0; i < cats.size() - 1; i++) {
                            message += "[[:Category:" + cats.get(i) + "|" + cats.get(i) + "]]";
                        }
                        message += "[[:Category:" + cats.get(cats.size() - 1) + "|" + cats.get(cats.size() - 1) + "]].";

                        aivbot.getIPs().put(user, message);
                    }

                    if (params.get("AutoMark").equals("on") && !line.contains("<!-- Marked -->")) {
                        for (String mask : aivbot.getIPs().keySet()) {
                            if (mask.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(?:/\\d{1,2})?$")) {
                                SubnetUtils subnet = new SubnetUtils(mask);
                                subnet.setInclusiveHostCount(true);
                                if (subnet.getInfo().isInRange(user)) {
                                    ipCommentsNeeded.add(Arrays.asList(new String[]{page, user, mask}));
                                    break;
                                }
                            } else {
                                if (mask.equals(user)) {
                                    ipCommentsNeeded.add(Arrays.asList(new String[]{page, user, mask}));
                                    break;
                                }
                            }
                        }
                    }

                }

                for (List<String> ipParams : ipCommentsNeeded) {
                    this.bot.addJob(new CommentSpecialIPJob(ipParams.toArray(new String[2]), reportCount));
                }

                if (params.get("AutoBacklog").equals("on")) {
                    if ((reportCount >= new Integer(params.get("AddLimit")) && !backlog)
                            || (reportCount <= new Integer(params.get("RemoveLimit")) && backlog)) {
                        this.bot.addJob(new FixBacklogJob(page, reportCount, params));
                    }
                }
            }
        }*/
    }

    @Override
    public void doShutdown() {
    }

    private Map<String, String> parseParams(String parameter_str) {
        Map<String, String> out = new HashMap<String, String>();
        String[] split = parameter_str.split(" ");

        for (String def : "RemoveBlocked MergeDuplicates AutoMark FixInstructions AutoBacklog".split(" ")) {
            out.put(def, "off");
        }

        for (String splat : split) {
            String[] item = splat.split("=");
            if (item.length == 2) {
                out.put(item[0], item[1].toLowerCase());
            }
        }

        if (out.get("AutoBacklog").equals("on")) {
            if (!out.containsKey("AddLimit")) {
                out.put("AddLimit", "0");
            }
            if (!out.containsKey("RemoveLimit")) {
                out.put("RemoveLimit", "0");
            }
        }

        if (new Integer(out.get("AddLimit")) <= new Integer(out.get("RemoveLimit"))) {
            out.put("AutoBacklog", "off");
        }

        return out;
    }
}
