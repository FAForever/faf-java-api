package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Include(rootLevel = true, type = "hardwareInformation")
@Table(name = "uniqueid")
@DeletePermission(expression = "Prefab.Role.None")
@Setter
public class HardwareInformation {

  private int legacyId;
  private String hash;
  private String uuid;
  private String memSerialNumber;
  private String deviceId;
  private String manufacturer;
  private String name;
  private String processorId;
  private String smbiosbiosVersion;
  private String serialNumber;
  private String volumeSerialNumber;
  private List<Player> players;


  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getLegacyId() {
    return legacyId;
  }

  @Id
  @Column(name = "hash")
  @Size(max = 32)
  @NotNull
  public String getHash() {
    return hash;
  }

  @Column(name = "uuid")
  @NotNull
  public String getUuid() {
    return uuid;
  }

  @Column(name = "mem_SerialNumber")
  @NotNull
  public String getMemSerialNumber() {
    return memSerialNumber;
  }

  @Column(name = "deviceID")
  @NotNull
  public String getDeviceId() {
    return deviceId;
  }

  @Column(name = "manufacturer")
  @NotNull
  public String getManufacturer() {
    return manufacturer;
  }

  @Column(name = "name")
  @NotNull
  public String getName() {
    return name;
  }

  @Column(name = "processorId")
  @NotNull
  public String getProcessorId() {
    return processorId;
  }

  @Column(name = "SMBIOSBIOSVersion")
  @NotNull
  public String getSmbiosbiosVersion() {
    return smbiosbiosVersion;
  }

  @Column(name = "serialNumber")
  @NotNull
  public String getSerialNumber() {
    return serialNumber;
  }

  @Column(name = "volumeSerialNumber")
  @NotNull
  public String getVolumeSerialNumber() {
    return volumeSerialNumber;
  }

  @ManyToMany
  @JoinTable(name = "unique_id_users",
    joinColumns = @JoinColumn(name = "uniqueid_hash", referencedColumnName = "hash"),
    inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
  public List<Player> getPlayers() {
    return this.players;
  }
}
