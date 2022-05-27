package hu.akarnokd.theplanetcrafter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PositionEchoClient {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 200; i++) {
            try (Socket socket = new Socket("127.0.0.1", 22526)) {
                
                BufferedReader bin = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                
                Completable.mergeArray(
                        Completable.fromAction(() -> {
                            receive(bin, out);
                        }).subscribeOn(Schedulers.io()),
                        Completable.fromAction(() -> {
                            send(out);
                        }).subscribeOn(Schedulers.io())
                )
                .blockingSubscribe();
                break;
            } catch (IOException ex) {
                System.out.println("Host not ready");
                Thread.sleep(1000);
            }
        }
    }
    
    static void receive(BufferedReader bin, PrintWriter out) throws IOException {
        String s = null;
        
        while ((s = bin.readLine()) != null) {
            if (s.startsWith("PlayerPosition|")) {
                String[] parts = s.split("\\|");
                parts[1] = Float.toString(Float.parseFloat(parts[1]) + 3);
                
                String joint = String.join("|", parts);
                //System.out.println("SEND=" + joint);
                out.print(joint);
                out.print('\n');
                out.flush();
                if (out.checkError()) {
                    break;
                }   
            } else {
                System.out.println("RECV=" + s);
                
            }
        }
    }
    
    static void send(PrintWriter out) throws IOException {
        System.out.print("SEND=Login|Buddy|password\n");
        out.print("Login|Buddy|password\n");
        out.flush();
    }
}
