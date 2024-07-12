package tm;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class JtmFileSystemView extends FileSystemView {
    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        return Arrays.stream(super.getFiles(dir, useFileHiding)).filter(f -> !f.isFile() || f.getName().endsWith("." + JMystica.gameFileExtension)).toArray(File[]::new);
    }
    @Override
    public File createNewFolder(File containingDir) throws IOException {
        return FileSystemView.getFileSystemView().createNewFolder(containingDir);
    }
}
