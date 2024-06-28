package br.alkazuz.launcher.updater;

import br.alkazuz.launcher.updater.downloader.FileDownloader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Updater extends JFrame {
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public Updater() {
        setTitle("Sunshine | Iniciando Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        setLayout(null);
        initComponents();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Updater::new);
    }

    public void initComponents() {
        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(50, 370, 300, 30);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("Analisando...", SwingConstants.CENTER);
        statusLabel.setBounds(50, 400, 300, 30);

        add(progressBar, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.CENTER);

        drawLogo();

        startDownloads();
    }

    private void drawLogo() {
        JLabel imageLogo = new JLabel();
        int frameWidth = 300;
        int frameHeight = 200;
        imageLogo.setBounds(0, 0, frameWidth, frameHeight);

        BufferedImage img = null;
        try {
            InputStream in = getClass().getResourceAsStream("/sunshine-logo.png");
            img = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (img != null) {
            int imgWidth = img.getWidth();
            int imgHeight = img.getHeight();

            double aspectRatio = (double) imgWidth / imgHeight;
            int newWidth, newHeight;

            if (frameWidth / frameHeight > aspectRatio) {
                newHeight = frameHeight;
                newWidth = (int) (newHeight * aspectRatio);
            } else {
                newWidth = frameWidth;
                newHeight = (int) (newWidth / aspectRatio);
            }

            Image dimg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            ImageIcon imageIcon = new ImageIcon(dimg);

            int x = 50;
            int y = 10;
            imageLogo.setBounds(x, y, newWidth, newHeight);
            imageLogo.setIcon(imageIcon);
        }

        add(imageLogo);
        revalidate();
        repaint();
    }

    private void startDownloads() {
        new FileDownloader(progressBar, statusLabel).downloadFileList();
    }


}
