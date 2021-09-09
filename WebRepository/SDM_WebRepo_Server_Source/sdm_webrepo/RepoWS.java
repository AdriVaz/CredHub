// 
// Decompiled by Procyon v0.5.36
// 

package sdm_webrepo;

import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.stream.Stream;
import java.io.IOException;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.nio.file.Path;
import java.io.File;
import java.util.List;
import java.nio.file.LinkOption;
import java.nio.file.Files;
import java.nio.file.FileVisitOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.jws.WebService;

@WebService(endpointInterface = "sdm_webrepo.IRepoWS")
public class RepoWS implements IRepoWS
{
    @Override
    public String[] ListCredentials() {
        try {
            final List<String> resultado = new ArrayList<String>();
            final Stream<Path> streamArchivos = Files.walk(Paths.get(".", new String[0]), new FileVisitOption[0]).filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0]));
            final List<File> listaArchivos = streamArchivos.map((Function<? super Path, ?>)Path::toFile).collect((Collector<? super Object, ?, List<File>>)Collectors.toList());
            streamArchivos.close();
            for (int i = 0; i < listaArchivos.size(); ++i) {
                final String filenameActual = listaArchivos.get(i).getName();
                if (filenameActual.endsWith(".cred")) {
                    resultado.add(filenameActual.substring(0, filenameActual.length() - 5));
                }
            }
            return resultado.toArray(new String[0]);
        }
        catch (IOException ex) {
            return new String[0];
        }
    }
    
    @Override
    public String[] ImportRecord(final String id) {
        try {
            final List<String> resultado = new ArrayList<String>();
            final Stream<Path> streamArchivo = Files.walk(Paths.get(".", new String[0]), new FileVisitOption[0]).filter(p -> p.toFile().getName().equals(id + ".cred"));
            final List<Path> listaArchivo = streamArchivo.collect((Collector<? super Path, ?, List<Path>>)Collectors.toList());
            streamArchivo.close();
            if (listaArchivo.size() == 0) {
                throw new IOException();
            }
            return Files.readAllLines(listaArchivo.get(0)).toArray(new String[0]);
        }
        catch (IOException ex) {
            return new String[] { "Record not found" };
        }
    }
    
    @Override
    public String ExportRecord(final String id, final String username, final String password) {
        try {
            final File f = new File(id + ".cred");
            if (f.exists()) {
                f.delete();
            }
            final BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.write(id);
            writer.write(System.getProperty("line.separator"));
            writer.write(username);
            writer.write(System.getProperty("line.separator"));
            writer.write(password);
            writer.close();
            return "OK";
        }
        catch (IOException ex) {
            return "Error - " + ex.getMessage();
        }
    }
}
