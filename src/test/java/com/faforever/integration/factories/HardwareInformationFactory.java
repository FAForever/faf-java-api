package com.faforever.integration.factories;

import com.faforever.api.data.domain.HardwareInformation;
import com.faforever.api.data.domain.Player;
import com.faforever.integration.TestDatabase;
import lombok.Builder;
import lombok.Singular;
import org.springframework.util.Assert;

import java.util.List;

import static junitx.framework.ComparableAssert.assertEquals;

public class HardwareInformationFactory {
  public static final String DEFAULT_VALUE = "-";

  @Builder
  private static HardwareInformation create(String hash, String uuid, String MemSerialNumber,
                                            String device, String manufacturer, String name,
                                            String processor, String biosVersion, String serialNumber,
                                            String volumeSerialNumber, @Singular List<Player> players, TestDatabase database) {
    Assert.notNull(database, "'database' must not be null");
    Assert.notNull(hash, "'hash' must not be null");
    long count = database.getHardwareInformationRepository().count();
    HardwareInformation entity = new HardwareInformation()
      .setHash(hash)
      .setUuid(uuid != null ? uuid : DEFAULT_VALUE)
      .setMemSerialNumber(MemSerialNumber != null ? MemSerialNumber : DEFAULT_VALUE)
      .setDeviceId(device != null ? device : DEFAULT_VALUE)
      .setManufacturer(manufacturer != null ? manufacturer : DEFAULT_VALUE)
      .setName(name != null ? name : DEFAULT_VALUE)
      .setProcessorId(processor != null ? processor : DEFAULT_VALUE)
      .setSmbiosbiosVersion(biosVersion != null ? biosVersion : DEFAULT_VALUE)
      .setSerialNumber(serialNumber != null ? serialNumber : DEFAULT_VALUE)
      .setVolumeSerialNumber(volumeSerialNumber != null ? volumeSerialNumber : DEFAULT_VALUE)
      .setPlayers(players);

    entity = database.getHardwareInformationRepository().save(entity);
    assertEquals(count + 1, database.getHardwareInformationRepository().count());
    return entity;
  }
}
