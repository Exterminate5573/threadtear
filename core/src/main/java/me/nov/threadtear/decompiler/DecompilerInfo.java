package me.nov.threadtear.decompiler;

import java.util.ArrayList;
import java.util.List;

public abstract class DecompilerInfo<T extends IDecompilerBridge> {

  public abstract String getName();

  public abstract String getVersionInfo();

  public abstract T createDecompilerBridge();

  @Override
  public String toString() {
    return getName() + " " + getVersionInfo();
  }

  public static List<DecompilerInfo<?>> getDecompilerInfos() {
    List<DecompilerInfo<?>> list = new ArrayList<>(4);

    list.add(new CFRBridge.CFRDecompilerInfo());
    list.add(new QuiltflowerBridge.QuiltflowerDecompilerInfo());
    list.add(new KrakatauBridge.KrakatauDecompilerInfo());

    //Procyon
    list.add(new ProcyonBridge.ProcyonDecompilerInfo());

    return list;
  }
}
