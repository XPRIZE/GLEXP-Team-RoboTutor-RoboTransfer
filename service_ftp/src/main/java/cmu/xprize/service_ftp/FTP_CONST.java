package cmu.xprize.service_ftp;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * FTP_CONST
 * <p>
 * Created by kevindeland on 2/14/18.
 */

public class FTP_CONST {
    public static final String FOLDER_NAME = "ftp_transfer"; // TODO how can we make this configgable?
    public static final String LOG_FILE_LOCATION = Environment.getExternalStorageDirectory().getPath() + File.separator + FOLDER_NAME + File.separator;

    public static final String FTP_WRITE_LOCATION = "/storage/emulated/0" + File.separator + FOLDER_NAME;


    /**
     * Holds config info for different FTP profiles, for example local testing or in the XPrize field
     *
     * address: FTP server address
     * user & pw: self-explanatory
     * folderPairs: a list of SOURCE -> DEST folder pairs, where files are read from tablet/SOURCE and written to ftp/DEST
     *
     */
    public static class FtpConfigProfile {

        public String address;
        public String user;
        public String pw;
        public int port;
        public List<FtpInOutFolderPair> folderPairs;

        public FtpConfigProfile(String address, String user, String pw, int port) {
            this.address = address;
            this.user = user;
            this.pw = pw;
            this.port = port;
            folderPairs = new ArrayList<>();
        }

        public static class FtpInOutFolderPair {
            public String source;
            public String dest;

            public FtpInOutFolderPair(String source, String dest) {
                this.source = source;
                this.dest = dest;
            }

        }

        public void addFolderPair(String source, String dest) {
            folderPairs.add(new FtpInOutFolderPair(source, dest));
        }

    }

    public static final FtpConfigProfile KEVIN_LOCAL_FTP = new FtpConfigProfile("128.237.127.125", "anonymous", null, 2121);
    static {
        KEVIN_LOCAL_FTP.addFolderPair("ftp_transfer", "/ftp_transfer");
    }

    public static final FtpConfigProfile KEVIN_LOCAL_FTP_2 = new FtpConfigProfile("128.237.127.125", "anonymous", null, 2121); // TODO backup, use "" as pw
    static {
        KEVIN_LOCAL_FTP_2.addFolderPair("ftp_transfer", "ftp_transfer");
    }

    // TODO for robustness... possibly make full list of all possible ports (21, 2121) and addresses, and check each of them???
    public static final FtpConfigProfile XPRIZE_FIELD_FTP = new FtpConfigProfile("192.168.0.1", "anonymous", "", 21);
    static {
        XPRIZE_FIELD_FTP.addFolderPair("ftp_transfer", "/remote");
    }

    static final long BACKGROUND_CHECK_DELAY = 0;
    static final long BACKGROUND_CHECK_PERIOD = 5000;



}
