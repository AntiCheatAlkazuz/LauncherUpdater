package br.alkazuz.launcher.updater.downloader;

import br.alkazuz.launcher.updater.object.FileEntry;
import br.alkazuz.launcher.updater.util.MD5Checksum;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
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
                    File localFile = new File(file.getFile());
                    if (localFile.exists() && !file.getHash().equals(MD5Checksum.calculateMD5(localFile))) {
                        updateFile(file.getFile().substring(2));
                    } else if (!localFile.exists()) {
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
                    runLauncher(new File("."));
                    System.exit(0);
                }
            }
        };
        worker.execute();
    }

    private void runLauncher(File dir) {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "bin/launcher.jar");
            pb.directory(dir);
            pb.start();
        } catch (IOException e) {
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
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();
    }
}

