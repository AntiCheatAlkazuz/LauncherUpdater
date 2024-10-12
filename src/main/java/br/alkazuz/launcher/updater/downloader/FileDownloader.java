package br.alkazuz.launcher.updater.downloader;

import br.alkazuz.launcher.updater.Updater;
import br.alkazuz.launcher.updater.object.FileEntry;
import br.alkazuz.launcher.updater.util.MD5Checksum;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class FileDownloader {
    private static final String BASE_URL = "https://coremc.com.br/updates/";
    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public FileDownloader(JProgressBar progressBar, JLabel statusLabel) {
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
    }

    public void downloadFileList() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                URL url = new URL(BASE_URL + "updates.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Sunshine AntiCheat");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                List<FileEntry> files = new Gson().fromJson(reader, new TypeToken<List<FileEntry>>(){}.getType());
                reader.close();
                int totalFiles = files.size();
                int currentFileIndex = 0;

                for (FileEntry file : files) {
                    File localFile = new File("bin" + File.separator + file.getFile());
                    if (localFile.exists() && !file.getHash().equals(MD5Checksum.calculateMD5(localFile))) {
                        System.out.println("md5 diferente: " + file.getFile());
                        updateFile(file.getFile().substring(2));
                    } else if (!localFile.exists()) {
                        System.out.println("arquivo nao existe: " + file.getFile());
                        updateFile(file.getFile().substring(2));
                    }
                    currentFileIndex++;
                    publish(String.format("Baixando %s (%d/%d)", file.getFile(), currentFileIndex, totalFiles));
                    progressBar.setValue((currentFileIndex * 100) / totalFiles);
                }
                return null;
            }

            @Override
            protected void process(List chunks) {
                statusLabel.setText((String) chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Todos os downloads foram completados.");
                } catch (Exception e) {
                    statusLabel.setText("Falha ao baixar arquivos.");
                    JOptionPane.showMessageDialog(null, "Falha ao baixar arquivos. Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    runLauncher();
                    System.exit(0);
                }
            }
        };
        worker.execute();
    }

    private void runLauncher() {
        try {
            File workingFolder = new File("bin");
            if (workingFolder.exists()) {
                if (!workingFolder.isDirectory())
                    workingFolder.mkdirs();
            } else {
                workingFolder.mkdirs();
            }

            String[] arguments = new String[Updater.args.length + 4];
            for (int i = 0; i < arguments.length; i++) {
                switch (i) {
                    case 1:
                        arguments[1] = "-Duser.home=" + workingFolder.getAbsolutePath();
                        break;
                    case 2:
                        arguments[2] = "-jar";
                        break;
                    case 3:
                        arguments[3] = workingFolder.getAbsolutePath() + File.separator + "launcher.jar";
                        break;
                }
            }
            File minecraftFolder = new File(workingFolder, "data");
            System.out.println("Minecraft folder: " + minecraftFolder.getAbsolutePath());
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "launcher.jar");
            Map<String, String> env = pb.environment();
            env.put("APPDATA", minecraftFolder.getAbsolutePath());
            pb.directory(new File("bin"));
            pb.start();

        } catch (IOException  e) {
            e.printStackTrace();
        }
    }

    private void updateFile(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Sunshine AntiCheat");
        InputStream in = connection.getInputStream();
        File file = new File("bin" + File.separator + path);
        file.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        System.out.println("Baixando " + path);
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();
    }
}

