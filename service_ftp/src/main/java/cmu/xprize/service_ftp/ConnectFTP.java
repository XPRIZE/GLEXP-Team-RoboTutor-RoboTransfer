package cmu.xprize.service_ftp;

import android.content.Context;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import cmu.xprize.service_ftp.logging.CLogManager;

/**
 * ConnectFTP
 * <p>
 * Created by kevindeland on 2/19/18.
 */

public class ConnectFTP {

    private static int CONNECT_TIMEOUT_IN_MS = 10 * 1000;
    private static int FTP_PORT = 2121;

    private static final String TAG = "ConnectFTP";
    private static final String DEBUG_TAG = "DEBUG_LAUNCH:FTP";


    private FTPClient mFtpClient = null;
    private Context mContext = null;

    public ConnectFTP() {
        mFtpClient = new FTPClient();
        this.mFtpClient.setConnectTimeout(CONNECT_TIMEOUT_IN_MS); // make sure our FTP connection doesn't hang
        this.mFtpClient.setDataTimeout(CONNECT_TIMEOUT_IN_MS);
    }

    /**
     * Connects with specified parameters
     *
     * @param ip
     * @param username
     * @param password
     * @return
     */
    public boolean connect(String ip, String username, String password, int port) {
        String log = String.format("Connecting to %s/%s with %s %s", ip, port, username, password);
        Log.w(DEBUG_TAG, log);
        CLogManager.getInstance().postEvent_I(TAG, log);
        boolean status = false;

        try {

            if (port < 0) {
                mFtpClient.connect(ip);
            } else {
                mFtpClient.connect(ip, port);
            }
            status = mFtpClient.login(username, password);
            Log.w(DEBUG_TAG, log + (status ? ": SUCCESS" : ": FAILED"));
            CLogManager.getInstance().postEvent_I(TAG, log + (status ? ": SUCCESS" : ": FAILED"));
        } catch (IOException e) {
            Log.w(DEBUG_TAG, "No FTP connection found");
            Log.w(DEBUG_TAG, e.getMessage());
            CLogManager.getInstance().postEvent_E(TAG, "No FTP connection found!");
            CLogManager.getInstance().postEvent_E(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }

        return status;
    }

    /**
     * STEP 10 Uploads a file to the FTP location
     * @param file
     * @return
     */
    public boolean uploadFile(File file, String location) {

        try {
            mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean dirExists = mFtpClient.changeWorkingDirectory(location);
            Log.w(DEBUG_TAG, "In directory: " + mFtpClient.printWorkingDirectory());
            CLogManager.getInstance().postEvent_I(TAG, "In directory: " + mFtpClient.printWorkingDirectory());
            if(!dirExists) {
                boolean dirCreated = mFtpClient.makeDirectory(location);

                if(dirCreated) {
                    Log.d(DEBUG_TAG, "Directory created: " + location);
                    CLogManager.getInstance().postEvent_I(TAG, "Directory created: " + location);
                } else {
                    Log.w(DEBUG_TAG, "Could not create directory: " + location);
                    CLogManager.getInstance().postEvent_I(TAG, "Could not create directory: " + location);
                    //return false;
                }
            } else {
                // screwy logic... go back up to parent directory
                mFtpClient.changeToParentDirectory();
            }


            String dir = mFtpClient.printWorkingDirectory();
            FTPFile[] dirs = mFtpClient.listDirectories();
            for (FTPFile ftpDir : dirs) {
                Log.d(DEBUG_TAG, ftpDir.getName());
            }
            Log.i(DEBUG_TAG, dir);
            CLogManager.getInstance().postEvent_I(TAG, dir);
            FileInputStream srcFileStream = new FileInputStream(file);

            String writeTo = location + File.separator + file.getName();

            boolean status = mFtpClient.storeFile(writeTo, srcFileStream);
            Log.e(DEBUG_TAG, "uploadFile status=" + status);
            CLogManager.getInstance().postEvent_I(TAG, "uploadFfile status=" + status);
            srcFileStream.close();
            return status;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * to be called after client is done uploading...s
     */
    public void disconnect() {
        CLogManager.getInstance().postEvent_I(TAG, "disconnecting from FTP...");

        try {
            mFtpClient.disconnect();
            CLogManager.getInstance().postEvent_I(TAG, "successfully disconnected");
        } catch (IOException e) {
            e.printStackTrace();
            CLogManager.getInstance().postEvent_E(TAG, "Error disconnecting... " + e.getMessage());
        }
    }
}
