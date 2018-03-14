package cmu.xprize.service_ftp;

import java.io.File;

/**
 * RoboSync
 * <p>
 *     A FilePair matches a File to read with a Directory to write that file to
 * Created by kevindeland on 3/14/18.
 */

public class FilePair {

    public File f;
    public String out_dir;

    FilePair(File f, String out_dir) {
        this.f = f;
        this.out_dir = out_dir;
    }
}
