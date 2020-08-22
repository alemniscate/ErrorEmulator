package correcter;

import java.util.Scanner;
import java.util.Random;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException{
        System.out.println("Write a mode: ");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        System.out.println();
        switch (command) {
            case "encode":
                new Encode();
                break;
            case "send":
                new Send();
                break;
            case "decode":
                new Decode();
                break;
        }
        scanner.close();
    }
}
 
class PrintText {

    PrintText(String fileName) throws IOException {
        File file = new File(fileName);

        FileReader inputC = new FileReader(file);
        StringWriter outputC = new StringWriter();

        int dataC = inputC.read();
        while (dataC != -1) {
            outputC.write(dataC);
            dataC = inputC.read();
        }

        String str = outputC.toString();
        Text t = new Text(str);
        t.print();

        inputC.close();
        outputC.close();
    }
}

class PrintByte {

    PrintByte(String fileName) throws IOException {
        File file = new File(fileName);

        FileInputStream inputB = new FileInputStream(file);
        ByteArrayOutputStream outputB = new ByteArrayOutputStream();

        int dataB = inputB.read();
        while (dataB != -1) {
            outputB.write(dataB);
            dataB = inputB.read();
        }

        byte[] bytes = outputB.toByteArray();
        Hex h = new Hex(bytes);
        h.print();
        Bin b = new Bin(bytes);
        b.print("bin view:");

        inputB.close();
        outputB.close();
    }
}

class Encode  {

    Encode() throws IOException {
        String sourceFileName = "send.txt";
        String destinationFileName = "encoded.txt";
        System.out.println(sourceFileName + ":");
        new PrintText(sourceFileName);
        new PrintByte(sourceFileName);

        System.out.println();
        System.out.println(destinationFileName + ":");
        File sendText = new File(sourceFileName);
        File encodedText = new File(destinationFileName);

        FileInputStream input = new FileInputStream(sendText);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileOutputStream outputF = new FileOutputStream(encodedText);

        int data = input.read();
        while (data != -1) {
            output.write(data);
            data = input.read();
        }

        byte[] bytes = output.toByteArray();
        int[] b2 = new int[bytes.length * 2];

        for (int i = 0, j = 0; i < bytes.length; i++, j +=2) {
            b2[j] = bytes[i] >>> 4;
            b2[j + 1] += bytes[i] & 15;
        }

        for (int i = 0; i < b2.length; i++) {
            int b = b2[i];

            int d3 = (b & 8) >> 3;
            int d5 = (b & 4) >> 2;
            int d6 = (b & 2) >> 1;
            int d7 = b & 1;

            int p1 = (d3 + d5 +d7) % 2 == 1 ? 1 : 0;
            int p2 = (d3 + d6 +d7) % 2 == 1 ? 1 : 0;
            int p4 = (d5 + d6 +d7) % 2 == 1 ? 1 : 0;
            int p8 = 0;

            b = p1 << 7;
            b += p2 << 6;
            b += d3 << 5;
            b += p4 << 4;
            b += d5 << 3;
            b += d6 << 2;
            b += d7 << 1;
            b += p8;

            b2[i] = b;
        }

        Bin bin = new Bin(b2, b2.length);
        bin.printExpand("expand:");
        bin.print("parity:");
        Hex h = new Hex(b2, b2.length);
        h.print();

        byte[] b2f = new byte[b2.length];
        for (int i = 0;i < b2.length; i++) {
            b2f[i] = (byte)b2[i];
        }
        outputF.write(b2f);

        input.close();
        output.close();
        outputF.close();
    }
}

class Send {

    Send() throws IOException {
        Random random = new Random();
        String sourceFileName = "encoded.txt";
        String destinationFileName = "received.txt";
  
        System.out.println(sourceFileName + ":");
        new PrintByte(sourceFileName);

        System.out.println();
        System.out.println(destinationFileName + ":");
        File encodedText = new File(sourceFileName);
        File receivedText = new File(destinationFileName);

        FileInputStream input = new FileInputStream(encodedText);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileOutputStream outputF = new FileOutputStream(receivedText);

        int data = input.read();
        while (data != -1) {
            output.write(data);
            data = input.read();
        }

        byte[] bytes = output.toByteArray();

        for (int i = 0; i < bytes.length; i ++) {
            int b = bytes[i];
            int pos = Math.abs(random.nextInt() % 8);
            int mask = 1 << pos;
            bytes[i] = (byte)(mask ^ b);
        }

        Bin b = new Bin(bytes);
        b.print("bin view:");
        Hex h = new Hex(bytes);
        h.print();

        outputF.write(bytes);

        input.close();
        output.close();
        outputF.close();
    }
}

class Decode {

    Decode() throws IOException {
        String sourceFileName = "received.txt";
        String destinationFileName = "decoded.txt";
  
        System.out.println(sourceFileName + ":");
        new PrintByte(sourceFileName);

        System.out.println();
        System.out.println(destinationFileName + ":");
        File receivedText = new File(sourceFileName);
        File decodedText = new File(destinationFileName);

        FileInputStream input = new FileInputStream(receivedText);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileOutputStream outputF = new FileOutputStream(decodedText);

        int data = input.read();
        while (data != -1) {
            output.write(data);
            data = input.read();
        }

        byte[] bytes = output.toByteArray();

        final int P1POS = 7;
        final int P2POS = 6;
        final int D3POS = 5;
        final int P4POS = 4;
        final int D5POS = 3;
        final int D6POS = 2;
        final int D7POS = 1;
        final int D8POS = 0;

        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];

            int p1 = (b & 128) >> 7;
            int p2 = (b & 64) >> 6;
            int d3 = (b & 32) >> 5;
            int p4 = (b & 16) >> 4;
            int d5 = (b & 8) >> 3;
            int d6 = (b & 4) >> 2;
            int d7 = (b & 2) >> 1;
            int d8 = b & 1;

            int p1sum = (p1 + d3 + d5 + d7) % 2;
            int p2sum = (p2 + d3 + d6 + d7) % 2;
            int p4sum = (p4 + d5 + d6 + d7) % 2;

//          p1sum p2sum p4sum   errbit
//          0     0     0       -
//          0     0     1       p4
//          0     1     0       p2
//          0     1     1       d6
//          1     0     0       p1
//          1     0     1       d5
//          1     1     0       d3
//          1     1     1       d7

            int[] errposTable = {0, P4POS, P2POS, D6POS, P1POS, D5POS, D3POS, D7POS};
            int errpos = errposTable[p1sum * 4 + p2sum * 2 + p4sum];

            int mask = 1 << errpos;
            b ^= mask;

            bytes[i] = (byte)b;
        }

        Bin bin = new Bin(bytes);
        bin.print("correct:");
 
        byte[] dbytes = new byte[bytes.length / 2];

        for (int i = 0, j = 0; i < bytes.length; i += 2, j++) {
            int b = 0; 
            b = (bytes[i] & 32) << 2;
            b += (bytes[i] & (8 + 4 + 2)) << 3;
            b += (bytes[i + 1] & 32) >> 2;
            b += (bytes[i + 1] & (8 + 4 + 2)) >> 1;
            dbytes[j] = (byte)b;
        }

        bin = new Bin(dbytes);
        bin.print("decode:");

        Hex h = new Hex(dbytes);
        h.print();

        outputF.write(dbytes);

        input.close();
        output.close();
        outputF.close();

        new PrintText(destinationFileName);
    }
}

class Text {
    String str;

    Text(String str) {
        this.str = str;
    }

    void print() {
        System.out.println("text view: " + str);
    }
}

class Hex {
    byte[] bytes;

    Hex(byte[] bytes) {
        this.bytes = bytes;
    }

    Hex(int[] ints, int length) {
        bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte)ints[i];
        }
    }

    void print() {
        System.out.print("hex view:");
        for (byte b: bytes) {
            System.out.printf(" %02X", b);
        }
        System.out.println();
    }
}

class Bin {
    byte[] bytes;

    Bin(byte[] bytes) {
        this.bytes = bytes;
    }

    Bin(int[] ints, int length) {
        bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte)ints[i];
        }
    }

    void print(String title) {
        System.out.print(title);
        for (byte b: bytes) {
            String s = "";
            byte mask = 1;
            for (int i = 0; i < 8; i++) {
                if ((b & mask) == 0) {
                    s = "0" + s;
                } else {
                    s = "1" + s;
                }
                mask <<= 1;
            }
            System.out.printf(" %s", s);
        }
        System.out.println();
    }

    void printExpand(String title) {
        System.out.print(title);
        for (int j = 0; j < bytes.length; j++) {
            byte b = bytes[j]; 
            String s = "";
            int mask = 1; 
            for (int i = 0; i < 8; i++) {
                if (i == 0 || i == 4 || i ==6 || i == 7) {
                    s = "." + s;
                } else {
                   if ((b & mask) == 0) {
                        s = "0" + s;
                    } else {
                     s = "1" + s;
                    }
                }
                mask <<= 1;
            }
            System.out.printf(" %s", s);
        }
        System.out.println();
    }
}
