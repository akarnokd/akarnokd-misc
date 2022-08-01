package hu.akarnokd.misc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

public class Finput {

    public static void main(String[] args) {
        var ipPath = Paths.get("");
        Flux.using(() -> new FileInputStream(ipPath.toFile()),
                fileInputStream -> {
                      final var isr = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                      final var bufferReader = new BufferedReader(isr);
                      return Flux.fromStream(bufferReader.lines());
                },
                is -> {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        throw Exceptions.bubble(ex);
                    }
                }
     );
    }
}
