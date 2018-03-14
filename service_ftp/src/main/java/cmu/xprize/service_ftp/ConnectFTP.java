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
import java.util.Properties;

/**
 * ConnectFTP
 * <p>
 * Created by kevindeland on 2/19/18.
 */

public class ConnectFTP {

    private static int CONNECT_TIMEOUT_IN_MS = 10 * 1000;
    private static int FTP_PORT = 2121;

    private static final String TAG = "ConnectFTP";

    private FTPClient mFtpClient = null;
    private Context mContext = null;

    public ConnectFTP() {
        mFtpClient = new FTPClient();
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
        String log = String.format("Connecting to %s/%s with %s:%s", ip, port, username, password);
        Log.w(TAG, log);
        boolean status = false;

        try {
            mFtpClient.connect(ip, port); // TODO is port optional?
            status = mFtpClient.login(username, password);
        } catch (IOException e) {
            Log.w(TAG, "No FTP connection found");
            Log.w(TAG, e.getMessage());
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
            if(!dirExists) {
                boolean dirCreated = mFtpClient.makeDirectory(location);

                if(dirCreated) {
                    Log.d(TAG, "Directory created: " + location);
                } else {
                    Log.w(TAG, "Could not create directory: " + location);
                    //return false;
                }
            } else {
                // screwy logic... go back up to parent directory
                mFtpClient.changeToParentDirectory();
            }


            String dir = mFtpClient.printWorkingDirectory();
            FTPFile[] dirs = mFtpClient.listDirectories();
            for (FTPFile ftpDir : dirs) {
                Log.d(TAG, ftpDir.getName());
            }
            Log.i(TAG, dir);
            FileInputStream srcFileStream = new FileInputStream(file);

            // TODO this should be written to a specified directory, not just the same name as the file
            //String writeTo = location + File.separator + file.getName();
            String writeTo = location + File.separator + file.getName();

            boolean status = mFtpClient.storeFile(writeTo, srcFileStream);
            Log.e(TAG, "uploadFile status=" + status);
            srcFileStream.close();
            return status;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
