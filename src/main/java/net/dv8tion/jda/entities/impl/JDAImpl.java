/**
 *    Copyright 2015 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.entities.impl;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.hooks.EventManager;
import net.dv8tion.jda.requests.WebSocketClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.impl.Log4JLogger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;


/**
 * Represents the core of the Discord API. All functionality is connected through this.
 */
public class JDAImpl extends JDA
{
    private final Map<String, User> userMap = new HashMap<>();
    private final Map<String, Guild> guildMap = new HashMap<>();
    private final Map<String, TextChannel> channelMap = new HashMap<>();
    private final Map<String, VoiceChannel> voiceChannelMap = new HashMap<>();
    private final Map<String, String> offline_pms = new HashMap<>();    //Userid -> channelid
    private final EventManager eventManager = new EventManager();
    private SelfInfo selfInfo = null;
    private String authToken = null;
    private WebSocketClient client;
    private int responseTotal;

    public JDAImpl()
    {

    }

    /**
     * Attempts to login to Discord.
     * Upon successful auth with Discord, a token is generated and stored in token.json.
     *
     * @param email
     *          The email of the account attempting to log in.
     * @param password
     *          The password of the account attempting to log in.
     * @throws IllegalArgumentException
     *          Thrown if this email or password provided are empty or null.
     * @throws LoginException
     *          Thrown if the email-password combination fails the auth check with the Discord servers.
     */
    @Override
    public void login(String email, String password) throws IllegalArgumentException, LoginException
    {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            throw new IllegalArgumentException("The provided email or password as empty / null.");

        Path tokenFile = Paths.get("tokens.json");
        JSONObject configs = null;
        String gateway = null;
        if (Files.exists(tokenFile))
        {
            configs = readJson(tokenFile);
        }
        if (configs == null)
        {
            configs = new JSONObject().put("tokens", new JSONObject()).put("version", 1);
        }

        Unirest.setDefaultHeader("Content-Type", "application/json");

        if (configs.getJSONObject("tokens").has(email))
        {
            try
            {
                authToken = configs.getJSONObject("tokens").getString(email);
                Unirest.setDefaultHeader("authorization", authToken);
                gateway = Unirest.get("https://discordapp.com/api/gateway").asJson().getBody().getObject().getString("url");
                System.out.println("Using cached Token: " + authToken);
            }
            catch (JSONException ex)
            {
                System.out.println("Token-file misformatted. Please delete it for recreation");
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (gateway == null)                                    //no token saved or invalid
        {
            try
            {
                Unirest.clearDefaultHeaders();
                String response = Unirest.post("https://discordapp.com/api/auth/login")
                        .body(new JSONObject().put("email", email).put("password", password).toString())
                        .asString().getBody();

                if (response == null || response.isEmpty())
                    throw new LoginException("The provided email / password combination was incorrect. Please provide valid details.");
                System.out.println("Login Successful!"); //TODO: Replace with Logger.INFO

                authToken = new JSONObject(response).getString("token");
                configs.getJSONObject("tokens").put(email, authToken);
                System.out.println("Created new Token: " + authToken);

                Unirest.setDefaultHeader("authorization", authToken);
                gateway = Unirest.get("https://discordapp.com/api/gateway").asJson().getBody().getObject().getString("url");
            }
            catch (UnirestException ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            System.out.println("Login Successful!"); //TODO: Replace with Logger.INFO
        }

        writeJson(tokenFile, configs);
        client = new WebSocketClient(gateway, this);
    }

    /**
     * Takes a provided json file, reads all lines and constructs a {@link org.json.JSONObject JSONObject} from it.
     *
     * @param file
     *          The json file to read.
     * @return
     *      The {@link org.json.JSONObject JSONObject} representation of the json in the file.
     */
    private static JSONObject readJson(Path file)
    {
        try
        {
            return new JSONObject(StringUtils.join(Files.readAllLines(file, StandardCharsets.UTF_8), ""));
        }
        catch (IOException e)
        {
            System.out.println("Error reading token-file. Defaulting to standard");
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            System.out.println("Token-file misformatted. Creating default one");
        }
        return null;
    }

    /**
     * Writes the json representation of the provided {@link org.json.JSONObject JSONObject} to the provided file.
     *
     * @param file
     *          The file which will have the json representation of object written into.
     * @param object
     *          The {@link org.json.JSONObject JSONObject} to write to file.
     */
    private static void writeJson(Path file, JSONObject object)
    {
        try
        {
            Files.write(file, Arrays.asList(object.toString(4).split("\n")), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e)
        {
            System.out.println("Error creating token-file");
        }
    }

    @Override
    public String getAuthToken()
    {
        return authToken;
    }

    @Override
    public void addEventListener(EventListener listener)
    {
        getEventManager().register(listener);
    }

    @Override
    public void removeEventListener(EventListener listener)
    {
        getEventManager().unregister(listener);
    }

    public EventManager getEventManager()
    {
        return eventManager;
    }

    public WebSocketClient getClient()
    {
        return client;
    }

    public Map<String, User> getUserMap()
    {
        return userMap;
    }

    @Override
    public List<User> getUsers()
    {
        List<User> users = new LinkedList<>();
        users.addAll(userMap.values());
        return Collections.unmodifiableList(users);
    }

    @Override
    public User getUserById(String id)
    {
        return userMap.get(id);
    }

    public Map<String, Guild> getGuildMap()
    {
        return guildMap;
    }

    @Override
    public List<Guild> getGuilds()
    {
        List<Guild> guilds = new LinkedList<>();
        guilds.addAll(guildMap.values());
        return Collections.unmodifiableList(guilds);
    }

    @Override
    public Guild getGuildById(String id)
    {
        return guildMap.get(id);
    }

    public Map<String, TextChannel> getChannelMap()
    {
        return channelMap;
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        List<TextChannel> tcs = new LinkedList<>();
        tcs.addAll(channelMap.values());
        return Collections.unmodifiableList(tcs);
    }

    @Override
    public TextChannel getTextChannelById(String id)
    {
        return channelMap.get(id);
    }

    public Map<String, VoiceChannel> getVoiceChannelMap()
    {
        return voiceChannelMap;
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        List<VoiceChannel> vcs = new LinkedList<>();
        vcs.addAll(voiceChannelMap.values());
        return Collections.unmodifiableList(vcs);
    }

    @Override
    public VoiceChannel getVoiceChannelById(String id)
    {
        return voiceChannelMap.get(id);
    }

    public Map<String, String> getOffline_pms()
    {
        return offline_pms;
    }

    /**
     * Returns the currently logged in account represented by {@link net.dv8tion.jda.entities.SelfInfo SelfInfo}.<br>
     * Account settings <b>cannot</b> be modified using this object. If you wish to modify account settings please
     *   use the AccountManager.
     *
     * @return
     *      The currently logged in account.
     */
    @Override
    public SelfInfo getSelfInfo()
    {
        return selfInfo;
    }

    public void setSelfInfo(SelfInfo selfInfo)
    {
        this.selfInfo = selfInfo;
    }

    @Override
    public int getResponseTotal()
    {
        return responseTotal;
    }

    public void setResponseTotal(int responseTotal)
    {
        this.responseTotal = responseTotal;
    }
}