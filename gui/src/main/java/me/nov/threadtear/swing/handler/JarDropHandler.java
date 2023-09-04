package me.nov.threadtear.swing.handler;

import java.awt.datatransfer.*;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.TransferHandler;

public class JarDropHandler extends TransferHandler {
  private static final long serialVersionUID = -1L;
  private static final Pattern PATTERN = Pattern.compile(".*(\\.jar|\\.class|\\.zip)");

  private final ILoader loader;

  public JarDropHandler(ILoader loader) {
    this.loader = loader;
  }

  @Override
  public boolean canImport(TransferHandler.TransferSupport info) {
    info.setShowDropLocation(false);
    return info.isDrop() && info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean importData(TransferHandler.TransferSupport info) {
    if (!info.isDrop()) return false;

    Transferable t = info.getTransferable();
    List<File> data;

    try {
      data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
    }
    catch (Exception e) {
      return false;
    }

    for (File file : data) {
      if (PATTERN.matcher(file.getName().toLowerCase()).matches()) {
        loader.onFileDrop(file);
        return true;
      }
    }

    return false;
  }
}
