/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.network;

import poker5cardgame.io.ComUtils;
import poker5cardgame.io.Writable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Multipurpose packet. Used to send and receive data from the network.
 * Contains a list of arguments that can be written to a stream or read by name
 * individually.
 *
 * @author Herman Dempere
 */
public class Packet implements Writable {

    public Network.Command command;
    private final List<Entry> entries;

    public Packet(Network.Command command) {
        this.command = command;
        entries = new ArrayList<>(8);
    }

    public void putField(java.lang.String name, Object o) {
        entries.add(new Entry(name, o));

    }

    public <T> T getField(java.lang.String name, Class<T> type) {
        for (Entry e : entries)
            if (e.name.equals(name))
                return type.cast(e.object);
        return null;
    }

    @Override
    public void write(ComUtils out) throws IOException {
        // Write command code
        out.write_string_pure(command.code);

        // Write a list of arguments
        for (Entry e : entries) {
            out.write_string_pure(" ");
            e.writable.write(out);
        }
    }

    /**
     * Entries inside a Packet. An ordered list of Arguments
     */
    private class Entry {

        java.lang.String name;
        Object object;
        Writable writable;

        protected Entry(java.lang.String name, Object o) {
            this.name = name;
            this.object = o;
            if (o instanceof java.lang.String)
                writable = new Writable.String((java.lang.String) o);
            if (o instanceof java.lang.Integer)
                writable = new Writable.Integer((java.lang.Integer) o);
            
        }
    }

}
