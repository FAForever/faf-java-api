package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "uniqueid")
@Include(name = UniqueId.TYPE_NAME, rootLevel = false)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class UniqueId extends AbstractEntity<UniqueId> implements Serializable {

  public static final String TYPE_NAME = "uniqueId";

  private String hash;
  private String uuid;
  private String memorySerialNumber;
  private String deviceId;
  private String manufacturer;
  private String name;
  private String processorId;
  private String SMBIOSBIOSVersion;
  private String serialNumber;
  private String volumeSerialNumber;


  @Column(name = "hash")
  @EqualsAndHashCode.Include
  @ToString.Include
  public String getHash() {
    return hash;
  }

  @Column(name = "uuid")
  public String getUuid() {
    return uuid;
  }

  @Column(name = "mem_SerialNumber")
  public String getMemorySerialNumber() {
    return memorySerialNumber;
  }

  @Column(name = "deviceID")
  public String getDeviceId() {
    return deviceId;
  }

  @Column(name = "manufacturer")
  public String getManufacturer() {
    return manufacturer;
  }

  @Column(name = "name")
  public String getName() {
    return name;
  }

  @Column(name = "processorId")
  public String getProcessorId() {
    return processorId;
  }

  @Column(name = "SMBIOSBIOSVersion")
  public String getSMBIOSBIOSVersion() {
    return SMBIOSBIOSVersion;
  }

  @Column(name = "serialNumber")
  public String getSerialNumber() {
    return serialNumber;
  }

  @Column(name = "volumeSerialNumber")
  public String getVolumeSerialNumber() {
    return volumeSerialNumber;
  }
}
