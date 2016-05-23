package hu.akarnokd.reactiverpc;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Protocol pattern:
 * <p>
 * <pre>
 * 00-03: payload length including the header (4 bytes little endian)
 * 04-04: entry type (1 bytes)
 * 05-07: entry flags (3 bytes little endian)
 * 08-0F: stream identifier (8 bytes little endian); stream 0 is reserved
 */
public enum RsRpcProtocol {
    ;
    /**
     * Starts a new stream numbered by the sender, the payload
     * is an UTF-8 function name if present
     */
    public static final int TYPE_NEW = 1;
    /**
     * Cancels and stops a stream, dropping all further messages
     * with the same identifier. The payload, if present may
     * contain a reason (UTF-8 error message with stacktrace).
     */
    public static final int TYPE_CANCEL = 2;
    /** The next value within a stream. */
    public static final int TYPE_NEXT = 3;
    /** 
     * The error signal. The payload, if present may
     * contain a reason (UTF-8 error message with stacktrace).
     */
    public static final int TYPE_ERROR = 4;
    /** The complete signal, stopping a stream. */
    public static final int TYPE_COMPLETE = 5;
    /** 
     * Indicate more values can be sent. If no payload present,
     * the flags holds the 3 byte positive integer amount,
     * if payload present, that indicates the request amount. Integer.MAX_VALUE and
     * negative amounts indicate unbounded mode. Zero is ignored in both cases.*/
    public static final int TYPE_REQUEST = 6;

    public interface RsRpcReceive {
        void onNew(long streamId, String function);
        
        void onCancel(long streamId, String reason);
        
        /**
         * Called when the stream contains an NEXT frame.
         * @param streamId the stream identifier
         * @param payload the payload bytes, its length is derived from the frame length
         * @param read the number of read bytes, allows deciding what to do for partial reads
         */
        void onNext(long streamId, byte[] payload, int read);
        
        void onError(long streamId, String reason);
        
        void onComplete(long streamId);
        
        void onRequested(long streamId, long requested);
        
        void onUnknown(int type, int flags, long streamId, byte[] payload, int read);
    }

    static void send(OutputStream out, long streamId, int type, int flags, byte[] payload) {
        try {
            int len = 16 + (payload != null ? payload.length : 0);
            
            out.write((len >> 0) & 0xFF);
            out.write((len >> 8) & 0xFF);
            out.write((len >> 16) & 0xFF);
            out.write((len >> 24) & 0xFF);
            
            out.write(type & 0xFF);
            
            out.write((flags >> 0) & 0xFF);
            out.write((flags >> 8) & 0xFF);
            out.write((flags >> 16) & 0xFF);

            out.write((int)(streamId >> 0) & 0xFF);
            out.write((int)(streamId >> 8) & 0xFF);
            out.write((int)(streamId >> 16) & 0xFF);
            out.write((int)(streamId >> 24) & 0xFF);
            out.write((int)(streamId >> 32) & 0xFF);
            out.write((int)(streamId >> 40) & 0xFF);
            out.write((int)(streamId >> 48) & 0xFF);
            out.write((int)(streamId >> 56) & 0xFF);
            
            if (payload != null && payload.length != 0) {
                out.write(payload);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    static void send(OutputStream out, long streamId, int type, int flags, long payload) {
        try {
            int len = 24;
            
            out.write((len >> 0) & 0xFF);
            out.write((len >> 8) & 0xFF);
            out.write((len >> 16) & 0xFF);
            out.write((len >> 24) & 0xFF);
            
            out.write(type & 0xFF);
            
            out.write((flags >> 0) & 0xFF);
            out.write((flags >> 8) & 0xFF);
            out.write((flags >> 16) & 0xFF);

            out.write((int)(streamId >> 0) & 0xFF);
            out.write((int)(streamId >> 8) & 0xFF);
            out.write((int)(streamId >> 16) & 0xFF);
            out.write((int)(streamId >> 24) & 0xFF);
            out.write((int)(streamId >> 32) & 0xFF);
            out.write((int)(streamId >> 40) & 0xFF);
            out.write((int)(streamId >> 48) & 0xFF);
            out.write((int)(streamId >> 56) & 0xFF);

            out.write((int)(payload >> 0) & 0xFF);
            out.write((int)(payload >> 8) & 0xFF);
            out.write((int)(payload >> 16) & 0xFF);
            out.write((int)(payload >> 24) & 0xFF);
            out.write((int)(payload >> 32) & 0xFF);
            out.write((int)(payload >> 40) & 0xFF);
            out.write((int)(payload >> 48) & 0xFF);
            out.write((int)(payload >> 56) & 0xFF);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    static final byte[] EMPTY = new byte[0];

    public static void receive(InputStream in, RsRpcReceive onReceive) {
        try {
            int b1 = in.read();
            int b2 = in.read();
            int b3 = in.read();
            int b4 = in.read();
            
            if ((b1 < 0) || (b2 < 0) || (b3 < 0) || (b4 < 0)) {
                onReceive.onError(-1, "Channel/Connection closed (@ len)");
                return;
            }
            
            int len = (b1) | (b2 << 8) | (b3 << 16) | (b4 << 24);
            
            int type = in.read();
            
            if (type < 0) {
                onReceive.onError(-1, "Channel/Connection closed (@ type)");
                return;
            }
            
            b1 = in.read();
            b2 = in.read();
            b3 = in.read();
            
            if ((b1 < 0) || (b2 < 0) || (b3 < 0)) {
                onReceive.onError(-1, "Channel/Connection closed (@ flags)");
                return;
            }
            
            int flags = (b1) | (b2 << 8) | (b3 << 16);
            
            b1 = in.read();
            b2 = in.read();
            b3 = in.read();
            b4 = in.read();
            int b5 = in.read();
            int b6 = in.read();
            int b7 = in.read();
            int b8 = in.read();

            if ((b1 < 0) || (b2 < 0) || (b3 < 0) || (b4 < 0)
                    || (b5 < 0) || (b6 < 0) || (b7 < 0) || (b8 < 0)) {
                onReceive.onError(-1, "Channel/Connection closed (@ streamId)");
                return;
            }
            
            long streamId = (b1) | (b2 << 8) | (b3 << 16) | (((long)b4) << 24)
                    | (((long)b5) << 32) | (((long)b6) << 40) | (((long)b7) << 48) | (((long)b8) << 56);
            
            switch (type) {
            case TYPE_NEW: {
                if (len > 16) {
                    String function = readString(in, len - 16);
                    onReceive.onNew(streamId, function);
                } else {
                    onReceive.onNew(streamId, "");
                }
            }
            case TYPE_CANCEL: {
                if (len > 16) {
                    String reason = readString(in, len - 16);
                    onReceive.onCancel(streamId, reason);
                } else {
                    onReceive.onCancel(streamId, "");
                }
            }
            
            case TYPE_NEXT: {
                if (len > 16) {
                    byte[] payload = new byte[len - 16];
                    int r = in.read(payload);
                    onReceive.onNext(streamId, payload, r);
                } else {
                    onReceive.onNext(streamId, EMPTY, 0);
                }
            }
            case TYPE_ERROR: {
                if (len > 16) {
                    String reason = readString(in, len - 16);
                    onReceive.onError(streamId, reason);
                } else {
                    onReceive.onError(streamId, "");
                }
            }
            
            case TYPE_COMPLETE: {
                // ignore payload
                while (len > 16) {
                    if (in.read() < 0) {
                        break;
                    }
                }
                onReceive.onComplete(streamId);
            }
            
            case TYPE_REQUEST: {
                if (len > 16) {
                    b1 = in.read();
                    b2 = in.read();
                    b3 = in.read();
                    b4 = in.read();
                    b5 = in.read();
                    b6 = in.read();
                    b7 = in.read();
                    b8 = in.read();
                    
                    if ((b1 < 0) || (b2 < 0) || (b3 < 0) || (b4 < 0)
                            || (b5 < 0) || (b6 < 0) || (b7 < 0) || (b8 < 0)) {
                        onReceive.onError(streamId, "Channel/Connection closed (@ request)");
                        return;
                    }
                    
                    long requested = (b1) | (b2 << 8) | (b3 << 16) | (((long)b4) << 24)
                            | (((long)b5) << 32) | (((long)b6) << 40) | (((long)b7) << 48) | (((long)b8) << 56);
                    
                    onReceive.onRequested(streamId, requested);
                } else {
                    onReceive.onRequested(streamId, flags);
                }
            }
            
            default: {
                if (len > 16) {
                    byte[] payload = new byte[len - 16];
                    int r = in.read(payload);
                    onReceive.onUnknown(type, flags, streamId, payload, r);
                } else {
                    onReceive.onUnknown(type, flags, streamId, EMPTY, 0);
                }
            }
            }
            
        } catch (IOException ex) {
            onReceive.onError(-1, "I/O error while reading data: " + ex);
        }
    }
    
    static String readString(InputStream in, int payloadLength) throws IOException {
        StringBuilder sb = new StringBuilder(payloadLength);
        
        for (;;) {
            if (payloadLength == 0) {
                break;
            }
            int b = in.read();

            payloadLength--;

            if (b < 0) {
                break;
            } else
            if ((b & 0x80) == 0) {
                sb.append((char)b);
            } else
            if ((b & 0b1110_0000) == 0b1100_0000) {
                
                if (payloadLength-- == 0) {
                    break;
                }
                
                int b1 = in.read();
                if (b1 < 0) {
                    break;
                }
                
                int c = ((b & 0b1_1111) << 6) | (b1 & 0b0011_1111);
                sb.append((char)c);
            } else
            if ((b & 0b1111_0000) == 0b1110_0000) {
                if (payloadLength-- == 0) {
                    break;
                }
                
                int b1 = in.read();

                if (b1 < 0) {
                    break;
                }
                
                if (payloadLength-- == 0) {
                    break;
                }
                
                int b2 = in.read();

                if (b2 < 0) {
                    break;
                }
                
                int c = ((b & 0b1111) << 12) | ((b1 & 0b11_1111) << 6)
                        | ((b2 & 0b11_1111));
                
                sb.append((char)c);
            } else
            if ((b & 0b1111_1000) == 0b1111_0000) {
                if (payloadLength-- == 0) {
                    break;
                }
                
                int b1 = in.read();

                if (b1 < 0) {
                    break;
                }
                
                if (payloadLength-- == 0) {
                    break;
                }
                
                int b2 = in.read();

                if (b2 < 0) {
                    break;
                }

                if (payloadLength-- == 0) {
                    break;
                }
                
                int b3 = in.read();

                if (b3 < 0) {
                    break;
                }

                int c = ((b & 0b111) << 18) 
                        | ((b1 & 0b11_1111) << 12)
                        | ((b1 & 0b11_1111) << 6)
                        | ((b2 & 0b11_1111));
                
                sb.append((char)c);
            }
        }
        
        return sb.toString();
    }
    
    static byte[] utf8(String s) {
        if (s == null || s.isEmpty()) {
            return EMPTY;
        }
        return s.getBytes(StandardCharsets.UTF_8);
    }
    
    public static void open(OutputStream out, long streamId, String functionName) {
        send(out, streamId, TYPE_NEW, 0, utf8(functionName));
    }
    
    public static void cancel(OutputStream out, long streamId, String reason) {
        send(out, streamId, TYPE_CANCEL, 0, utf8(reason));
    }
    
    public static void cancel(OutputStream out, long streamId, Throwable reason) {
        send(out, streamId, TYPE_CANCEL, 0, errorBytes(reason));
    }
    
    public static byte[] errorBytes(Throwable reason) {
        if (reason == null) {
            return EMPTY;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        reason.printStackTrace(new PrintWriter(new OutputStreamWriter(bout, StandardCharsets.UTF_8)));
        return bout.toByteArray();
    }
    
    public static void next(OutputStream out, long streamId, byte[] data) {
        send(out, streamId, TYPE_NEXT, 0, data);
    }
    
    public static void next(OutputStream out, long streamId, String text) {
        next(out, streamId, utf8(text));
    }
    
    public static void error(OutputStream out, long streamId, String reason) {
        send(out, streamId, TYPE_ERROR, 0, utf8(reason));
    }
    
    public static void error(OutputStream out, long streamId, Throwable reason) {
        send(out, streamId, TYPE_ERROR, 0, errorBytes(reason));
    }
    
    public static void complete(OutputStream out, long streamId) {
        send(out, streamId, TYPE_COMPLETE, 0, EMPTY);
    }
    
    static final byte[] REQUEST_UNBOUNDED = { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, 
            (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x7F };
    
    public static void request(OutputStream out, long streamId, long requested) {
        if (requested < 0 || requested == Long.MAX_VALUE) {
            send(out, streamId, TYPE_REQUEST, 0, REQUEST_UNBOUNDED);
        } else
        if (requested <= 0xFFFFFF) {
            send(out, streamId, TYPE_REQUEST, (int)requested & 0xFFFFFF, EMPTY);
        } else {
            send(out, streamId, TYPE_REQUEST, 0, requested);
        }
    }
    
}
