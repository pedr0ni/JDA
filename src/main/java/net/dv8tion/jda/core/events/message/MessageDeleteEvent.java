/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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
package net.dv8tion.jda.core.events.message;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

/**
 * <b><u>MessageDeleteEvent</u></b><br>
 * Fired if a Message was deleted in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.<br>
 * <br>
 * Use: Detect when a Message is deleted. No matter if private or guild.
 *
 * <p><b>JDA does not have a cache for messages and is not able to provide previous information due to limitations by the
 * Discord API!</b>
 */
public class MessageDeleteEvent extends GenericMessageEvent
{

    public MessageDeleteEvent(JDA api, long responseNumber, long messageId, MessageChannel channel)
    {
        super(api, responseNumber, messageId, channel);
    }

    public PrivateChannel getPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) channel : null;
    }

    public Group getGroup()
    {
        return isFromType(ChannelType.GROUP) ? (Group) channel : null;
    }

    public TextChannel getTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) channel : null;
    }

    public Guild getGuild()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getGuild() : null;
    }
}
