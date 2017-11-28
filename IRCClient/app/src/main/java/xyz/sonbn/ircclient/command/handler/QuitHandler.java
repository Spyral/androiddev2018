/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

This file is part of Yaaic.

Yaaic is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Yaaic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.
 */
package xyz.sonbn.ircclient.command.handler;

import android.content.Context;

import xyz.sonbn.ircclient.R;
import xyz.sonbn.ircclient.command.BaseHandler;
import xyz.sonbn.ircclient.irc.CommandException;
import xyz.sonbn.ircclient.irc.IRCService;
import xyz.sonbn.ircclient.model.Conversation;
import xyz.sonbn.ircclient.model.Server;

/**
 * Command: /quit [<reason>]
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class QuitHandler extends BaseHandler
{
    /**
     * Execute /quit
     */
    @Override
    public void execute(String[] params, Server server, Conversation conversation, IRCService service) throws CommandException
    {
        if (params.length == 1) {
            service.getConnection(server.getId()).quitServer();
        } else {
            service.getConnection(server.getId()).quitServer(BaseHandler.mergeParams(params));
        }
    }

    /**
     * Usage of /quit
     */
    @Override
    public String getUsage()
    {
        return "/quit [<reason>]";
    }

    /**
     * Description of /quit
     */
    @Override
    public String getDescription(Context context)
    {
        return context.getString(R.string.command_desc_quit);
    }
}
