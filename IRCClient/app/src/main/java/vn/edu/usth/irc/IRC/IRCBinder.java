package vn.edu.usth.irc.IRC;

import android.os.Binder;

import vn.edu.usth.irc.Model.Server;

/**
 * Binder for service communication
 *
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class IRCBinder extends Binder
{
    private final IRCService service;

    /**
     * Create a new binder for given service
     *
     * @param service
     */
    public IRCBinder(IRCService service)
    {
        super();

        this.service = service;
    }

    /**
     * Connect to given server
     *
     * @param server
     */
    public void connect(final Server server)
    {
        service.connect(server);
    }

    /**
     * Get service associated with this binder
     *
     * @return
     */
    public IRCService getService()
    {
        return service;
    }
}
