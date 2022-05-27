package hu.akarnokd.theplanetcrafter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PositionEchoHost {

    public static void main(String[] args) throws Exception {
        try (ServerSocket socket = new ServerSocket(22526)) {
            while (true) {
                Socket sock = socket.accept();
                BufferedReader bin = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8));
                
                Completable.mergeArray(
                        Completable.fromAction(() -> {
                            receive(bin, out);
                        }).subscribeOn(Schedulers.io()),
                        Completable.fromAction(() -> {
                            send(out);
                        }).subscribeOn(Schedulers.io())
                )
                .blockingSubscribe();
            }
        }
    }
    
    static void receive(BufferedReader bin, PrintWriter out) throws IOException {
        String s = null;
        
        while ((s = bin.readLine()) != null) {
            System.out.println("RECV=" + s);
            
            if (s.startsWith("PlayerPosition|")) {
                String[] parts = s.split("\\|");
                parts[1] = Float.toString(Float.parseFloat(parts[1]) - 3);
                
                String joint = String.join("|", parts);
                System.out.println("SEND=" + joint);
                out.print(joint);
                out.print('\n');
                out.flush();
                if (out.checkError()) {
                    break;
                }
            }
            if (s.startsWith("Login|")) {
                System.out.print("SEND=Welcome\n");
                out.print("Welcome\n");
                out.flush();
            }
        }
    }
    
    static void send(PrintWriter out) throws IOException {
    }
}
