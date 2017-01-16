package com.faforever.api.utils;

public interface ByteCountListener {

  void updateBytesWritten(long written, long total);
}
