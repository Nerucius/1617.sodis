/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Akira
 */
public class BufferTest {

    public static void main(String... args) throws Exception {

        ByteBuffer buff = ByteBuffer.allocate(64);
        InputStream bis = new ByteArrayInputStream(buff.array());
        
        buff.put("ERRO 08MS".getBytes());
        readStream(bis);       

        System.out.println("--------------");
        
        buff.put("G HERE".getBytes());
        bis.reset();
        readStream(bis);    
                
        System.out.println("--------------");
        
        bis.reset();
        buff.clear();
        fillZero(buff.array());
        readStream(bis);   


    }

    static void readStream(InputStream is) throws Exception {
        byte b;
        while ((b = (byte) is.read()) > 0) {
            System.out.println((char) b);
        }
    }
    
    static void fillZero(byte[] arr){
        for(int i = arr.length-1; i >= 0; i--)
            arr[i] = (byte)0;
    }

}
