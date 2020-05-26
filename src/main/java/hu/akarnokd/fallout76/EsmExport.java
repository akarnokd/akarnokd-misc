package hu.akarnokd.fallout76;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.Inflater;

public class EsmExport {

    static PrintWriter save;

    public static void main(String[] args) throws Throwable {
        File file = new File(
                "e:\\Games\\Fallout76\\Data\\SeventySix.esm");

        save = new PrintWriter(new FileWriter("e:\\Games\\Fallout76\\Data\\SeventySix.txt"));
        try {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                while (raf.getFilePointer() < raf.length()) {
                    processTopGroups(raf, "LVLI");
                }
            }
        } finally {
            save.close();
        }
    }
    
    static String readChars(DataInput din, int count) throws IOException {
        char[] chars = new char[count];
        for (int i = 0; i < count; i++) {
            chars[i] = (char)din.readUnsignedByte();
        }
        return new String(chars);
    }
    
    static String intToChar(int v) {
        return "" + ((char)((v >> 0) & 0xFF))
                + ((char)((v >> 8) & 0xFF))
                + ((char)((v >> 16) & 0xFF))
                + ((char)((v >> 24) & 0xFF))
                ;
    }
    
    static int FLAGS_COMPRESSED = 0x00040000;
    
    static void processTopGroups(DataInput din, String filterGroup) throws Exception {
        System.out.println(":---");
        String type = readChars(din, 4);
        System.out.printf("Type: %s%n", type);
        int size = Integer.reverseBytes(din.readInt());
        System.out.printf("Size: %s%n", size);

        if ("GRUP".equals(type)) {
            int labelOf = Integer.reverseBytes(din.readInt());
            int gtype = Integer.reverseBytes(din.readInt());

            String groupLabel = "";
            if (gtype == 0) {
                groupLabel = intToChar(labelOf);
                System.out.printf("GroupType: Top%n", gtype);
                System.out.printf("Record type: %s%n", groupLabel);
            } else {
                System.out.printf("Label: %08X%n", labelOf);
                System.out.printf("GroupType: %08X%n", gtype);
            }
            // skip version control and unknown
            din.skipBytes(8);

            // data starts here

            if (filterGroup != null && filterGroup.contains(groupLabel)) {
                int offset = 0;
                while (offset < size - 24) {
                    offset += processRecords(din);
                }
            } else {
                din.skipBytes(size - 24);
            }
        } else {
            int flags = Integer.reverseBytes(din.readInt());
            System.out.printf("Flags: %08X%n", flags);
            if ((flags & FLAGS_COMPRESSED) != 0) {
                System.out.println("       Compressed");
            }
            System.out.printf("ID: %08X%n", Integer.reverseBytes(din.readInt()));
            // skip version control and unknown
            din.skipBytes(8);
            // data starts here
            din.skipBytes(size);
        }
    }
    static int processRecords(DataInput din) throws Exception {
        String type = readChars(din, 4);
        int size = Integer.reverseBytes(din.readInt());
        int flags = Integer.reverseBytes(din.readInt());
        boolean isCompressed = (flags & FLAGS_COMPRESSED) != 0;
        int id = Integer.reverseBytes(din.readInt());

        /*
        System.out.println("   ---");
        System.out.printf("   Type: %s%n", type);
        System.out.printf("   Size: %s%n", size);
        System.out.printf("   Flags: %08X%n", flags);
        if (isCompressed) {
            System.out.println("       Compressed");
        }
        System.out.printf("   ID: %08X%n", id);
        */
        
        // skip version control and unknown
        din.skipBytes(8);
        
        save.printf("%s %08X %d%n", type, id, flags);
        
        if (!isCompressed) {
            for (FieldEntry fe : processFields(din, size)) {
                //System.out.printf("      + %s%n", fe.asString(type));
                fe.printBinary(save, type);
            }
        } else {
            int decompressSize = Integer.reverseBytes(din.readInt());
            byte[] inputbuf = new byte[size - 4];
            din.readFully(inputbuf);
            
            Inflater inflater = new Inflater();   
            inflater.setInput(inputbuf); 
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressSize);
            byte[] buffer = new byte[1024];  
            while (!inflater.finished()) {  
             int count = inflater.inflate(buffer);  
             outputStream.write(buffer, 0, count);  
            }  
            outputStream.close();  
            byte[] output = outputStream.toByteArray();
            
            DataInput cdin = new DataInputStream(new ByteArrayInputStream(output));
            
            for (FieldEntry fe : processFields(cdin, size)) {
                //System.out.printf("      + %s%n", fe.asString(type));
                fe.printBinary(save, type);
            }
            // data starts here
            //din.skipBytes(size);
        }
        
        return size + 24;
    }
    
    static List<FieldEntry> processFields(DataInput din, int size) throws IOException {
        int offset = 0;
        List<FieldEntry> result = new ArrayList<>();
        while (offset < size) {
            String ftype = readChars(din, 4);
            int fsize = din.readUnsignedByte() + din.readUnsignedByte() * 256;
            
            byte[] data = new byte[fsize];
            din.readFully(data);
            offset += 6 + fsize;
            result.add(new FieldEntry(ftype, data));
        }
        return result;
    }
    
    static final class FieldEntry {
        final String type;
        final byte[] data;
        FieldEntry(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }
        
        String asString(String parentType) {
            String result = "";
            switch (type) {
            case "EDID": {
                result = "EDID: " + getZString();
                break;
            }
            case "CNAM": {
                if (!parentType.equals("KYWD")) {
                    result = "CNAM: " + getZString();
                }
                break;
            }
            case "DNAM": {
                result = "DNAM: " + getZString();
                break;
            }
            case "SNAM": {
                result = "SNAM: " + getZString();
                break;
            }
            default: 
                result = type + " (" + data.length + ")";
            }
            return result;
        }
        
        void printBinary(PrintWriter out, String parentType) {
            out.printf("  %s (%d): ", type, data.length);
            
            switch (type) {
                case "EDID": {
                    out.print(getZString());
                    break;
                }
                case "LLCT": {
                    out.print(data[0]);
                    break;
                }
                case "LVLO": {
                    out.printf("%08X (object)", toInt(data[0], data[1], data[2], data[3]));
                    break;
                }
                case "LVOG": {
                    out.printf("%08X (global)", toInt(data[0], data[1], data[2], data[3]));
                    break;
                }
                case "LVOC": {
                    out.printf("%08X (global)", toInt(data[0], data[1], data[2], data[3]));
                    break;
                }
                case "LVOT": {
                    out.printf("%08X (curve)", toInt(data[0], data[1], data[2], data[3]));
                    break;
                }
                case "LVOV": {
                    out.printf("%.5f", Float.intBitsToFloat(toInt(data[0], data[1], data[2], data[3])));
                    break;
                }
                case "LVIV": {
                    out.printf("%.5f", Float.intBitsToFloat(toInt(data[0], data[1], data[2], data[3])));
                    break;
                }
                case "LVLV": {
                    out.printf("%.5f", Float.intBitsToFloat(toInt(data[0], data[1], data[2], data[3])));
                    break;
                }
                case "LVLF": {
                    int f = 0;
                    if (data.length >= 1) {
                        f = data[0];
                    }
                    if (data.length >= 2) {
                        f += (data[1] & 0xFF) * 256;
                    }
                    out.printf("%d", f);
                    if ((f & 1) != 0) {
                        out.printf(" +level");
                    }
                    if ((f & 2) != 0) {
                        out.printf(" +each");
                    }
                    if ((f & 4) != 0) {
                        out.printf(" +all");
                    }
                    break;
                }
                case "CTDA": {
                    printConditionData(out);
                    break;
                }
                default: {
                    for (byte b : data) {
                        out.printf("%02X", b);
                    }
                }
            }
            out.println();
        }
        
        void printConditionData(PrintWriter out) {
            out.println();
            out.printf("    Operator:");
            if ((data[0] >> 5) == 0) {
                out.printf(" ==");
            }
            if ((data[0] >> 5) == 1) {
                out.printf(" !=");
            }
            if ((data[0] >> 5) == 2) {
                out.printf(" >");
            }
            if ((data[0] >> 5) == 3) {
                out.printf(" >=");
            }
            if ((data[0] >> 5) == 4) {
                out.printf(" <");
            }
            if ((data[0] >> 5) == 5) {
                out.printf(" <=");
            }
            if ((data[0] & 1) == 0) {
                out.printf(" AND");
            }
            if ((data[0] & 2) != 0) {
                out.printf(" Parameters");
            }
            if ((data[0] & 4) != 0) {
                out.printf(" Global");
            }
            out.println();
            int val = toInt(data[4], data[5], data[6], data[7]);
            if ((data[0] & 4) != 0) {
                out.printf("    Global value: %08X%n", val);
            } else {
                out.printf("    Float value: %.5f%n", Float.intBitsToFloat(val));
            }
            int findex = toInt(data[8], data[9]) + 4096;
            if (!FUNCTION_MAP.containsKey(findex)) {
                System.err.println("Unknown function: " + findex + " (" + (findex - 4096) + ")");
                FUNCTION_MAP.put(findex, "Unknown");
            }
            out.printf("    Function: %d (%s)%n", findex, FUNCTION_MAP.get(findex));
            out.printf("    Param1: %08X%n", toInt(data[12], data[13], data[14], data[15]));
            out.printf("    Param2: %08X%n", toInt(data[16], data[17], data[18], data[19]));
            
            out.print("    RunOn: ");
            switch (data[20]) {
                case 0: {
                    out.print("Subject");
                    break;
                }
                case 1: {
                    out.print("Target");
                    break;
                }
                case 2: {
                    out.printf("Reference %08X", toInt(data[24], data[25], data[26], data[27]));
                    break;
                }
                default:
                    out.printf("%08X", toInt(data[20], data[21], data[22], data[23]));
            }
        }
        
        static int toInt(byte b1, byte b2, byte b3, byte b4) {
            return (b1 & 0xFF) + ((b2 & 0xFF) << 8)
                    + ((b3 & 0xFF) << 16) + ((b4 & 0xFF) << 24);
        }
        
        static int toInt(byte b1, byte b2) {
            return (b1 & 0xFF) + ((b2 & 0xFF) << 8);
        }

        private String getZString() {
            return new String(data, 0, data.length - 1, StandardCharsets.ISO_8859_1);
        }
    }
    
    static final Map<Integer, String> FUNCTION_MAP = new HashMap<>();
    static {
        FUNCTION_MAP.put(4778, "WornHasKeyword");
        FUNCTION_MAP.put(4173, "GetRandomPercent");
        FUNCTION_MAP.put(4170, "GetGlobalValue");
        FUNCTION_MAP.put(4639, "GetQuestCompleted");
        FUNCTION_MAP.put(4675, "GetEquippedShout");
        
        FUNCTION_MAP.put(4544, "HasPerk");
        FUNCTION_MAP.put(4165, "GetIsRace");
        FUNCTION_MAP.put(4176, "GetLevel");
        FUNCTION_MAP.put(4154, "GetStage");
        FUNCTION_MAP.put(4659, "LocationHasRefType");

        FUNCTION_MAP.put(4110, "GetActorValue");
        FUNCTION_MAP.put(4143, "GetItemCount");
        FUNCTION_MAP.put(4656, "HasKeyword");
        FUNCTION_MAP.put(4101, "GetLocked");
        FUNCTION_MAP.put(4161, "GetLockLevel");

        FUNCTION_MAP.put(4168, "GetIsID");
        FUNCTION_MAP.put(4455, "GetInCurrentLoc");
        FUNCTION_MAP.put(4657, "HasRefType");
        FUNCTION_MAP.put(4097, "GetDistance");
        FUNCTION_MAP.put(4278, "GetEquipped");

        FUNCTION_MAP.put(4661, "GetIsEditorLocation");
        FUNCTION_MAP.put(4266, "GetDayOfWeek");


    }
}
