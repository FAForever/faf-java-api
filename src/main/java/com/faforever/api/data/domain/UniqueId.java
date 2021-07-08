package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "uniqueid")
@Include(name = UniqueId.TYPE_NAME, rootLevel = false)
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class UniqueId implements DefaultEntity, Serializable {
  public static final String TYPE_NAME = "uniqueId";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  @ToString.Include
  private Integer id;

  @Column(name = "hash")
  @EqualsAndHashCode.Include
  @ToString.Include
  private String hash;

  @Column(name = "uuid")
  private String uuid;

  @Column(name = "mem_SerialNumber")
  private String memorySerialNumber;

  @Column(name = "deviceID")
  private String deviceId;

  @Column(name = "manufacturer")
  private String manufacturer;

  @Column(name = "name")
  private String name;

  @Column(name = "processorId")
  private String processorId;

  @Column(name = "SMBIOSBIOSVersion")
  private String SMBIOSBIOSVersion;

  @Column(name = "serialNumber")
  private String serialNumber;

  @Column(name = "volumeSerialNumber")
  private String volumeSerialNumber;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;
}
