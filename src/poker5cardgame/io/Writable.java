/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.io;

import java.io.IOException;

/**
 * Interface for network arguments. All classes implement a write method that
 * correctly writes the argument in whatever form is specified to a ComUtils
 * instance.
 */
public interface Writable {

    public void write(ComUtils out) throws IOException;

    /**
     * Writable String, can write itself, byte by byte onto the Stream.
     */
    public static class String implements Writable {

        private java.lang.String str;

        public String(java.lang.String str) {
            this.str = str;
        }

        @Override
        public void write(ComUtils out) throws IOException {
            out.write_string_pure(str);
        }

        @Override
        public java.lang.String toString() {
            return str;
        }

    }

    /**
     * Writable String, can write itself, byte by byte onto the Stream.
     */
    public static class VariableString implements Writable {

        private java.lang.String str;
        private int len;

        public VariableString(int len, java.lang.String str) {
            this.len = len;
            this.str = str;
        }

        @Override
        public void write(ComUtils out) throws IOException {
            out.write_string_variable(len, str);
        }

        @Override
        public java.lang.String toString() {
            return str;
        }

    }

    /**
     * Writable Integer, can write itself, in a single 4 bytes operation, onto
     * the Stream.
     */
    public static class Integer implements Writable {

        private java.lang.Integer i;

        public Integer(java.lang.Integer i) {
            this.i = i;
        }

        @Override
        public void write(ComUtils out) throws IOException {
            out.write_int32(i.intValue());
        }

        @Override
        public java.lang.String toString() {
            return i + "";
        }

    }

}
