/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.bot.entities.impl;

import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.apache.http.HttpHost;

public class JDABotImpl extends JDAImpl implements JDABot
{

    public JDABotImpl(HttpHost proxy, boolean autoReconnect, boolean audioEnabled, boolean useShutdownHook, boolean bulkDeleteSplittingEnabled)
    {
        super(proxy, autoReconnect, audioEnabled, useShutdownHook, bulkDeleteSplittingEnabled);
    }

    @Override
    public AccountType getAccountType()
    {
        return AccountType.BOT;
    }

    @Override
    public JDABot asBot()
    {
        return this;
    }
}
