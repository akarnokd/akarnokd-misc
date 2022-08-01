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
                out.print("Constructs|209794798;EscapePod;0;;598.74,1.32,682.76;0.01,-0.49,0.01,0.87;;;;0|105036705;Magnesium;0;;;;;;;0|207020435;SpaceMultiplierOxygen;3;;-500,-500,-500;0,0,0,1;;;;0|204703510;SpaceMultiplierHeat;4;;-500,-500,-500;0,0,0,1;;;;0|205385369;SpaceMultiplierPressure;5;;-500,-500,-500;0,0,0,1;;;;0|209166350;SpaceMultiplierBiomass;6;;-500,-500,-500;0,0,0,1;;;;0|104659988;Container1;104659988;;907.2864,28.85556,48.36556;0.2354753,-0.2263354,0.0370391,0.944432;;...;;0|202378668;Iridium;0;;;;;;;0|202255064;Aluminium;0;;;;;;;0|208184984;Alloy;0;;;;;;;0|204780199;FabricBlue;0;;;;;;;0|205557023;astrofood;0;;;;;;;0|106606665;Container1;106606665;;908.7042,27.96467,51.12227;0.2148088,0.3484681,0.3030255,0.8605829;;...;;0|202048292;Aluminium;0;;;;;;;0|206113605;Aluminium;0;;;;;;;0|208869546;Seed2;0;;;;;;;0|207709924;FabricBlue;0;;;;;;;0|204015919;FabricBlue;0;;;;;;;0\r\n");
                out.flush();
            }
        }
    }
    
    static void send(PrintWriter out) throws IOException {
    }
}
