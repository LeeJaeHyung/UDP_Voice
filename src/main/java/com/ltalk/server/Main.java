package com.ltalk.server;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.*;

public class Main {

    static DatagramSocket sendSocket;
    static DatagramSocket receiveSocket;

    public static void main(String[] args) throws SocketException {
        new Thread(() -> {
            try {
                receive();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                send();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }

    public static void send() throws IOException, LineUnavailableException {
        sendSocket = new DatagramSocket(0);
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
        mic.open(format);
        mic.start();

        DatagramSocket socket = new DatagramSocket();
        InetAddress receiverAddress = InetAddress.getByName("127.0.0.1"); // 상대방 IP
        int port = 5555;

        byte[] buffer = new byte[320];

        System.out.println("전송 시작");
        while (true) {
            int count = mic.read(buffer, 0, buffer.length);
            if (count > 0) {
                DatagramPacket packet = new DatagramPacket(buffer, count, receiverAddress, port);
                socket.send(packet);
            }
        }
    }

    public static void receive() throws IOException, LineUnavailableException {
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
        speaker.open(format);
        speaker.start();

        receiveSocket = new DatagramSocket(5555);
        byte[] buffer = new byte[320];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        System.out.println("수신 대기 중...");
        while (true) {
            receiveSocket.receive(packet);
            speaker.write(packet.getData(), 0, packet.getLength());
        }
    }
}