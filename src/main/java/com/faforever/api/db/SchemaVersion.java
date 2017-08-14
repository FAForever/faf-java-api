package com.faforever.api.db;

import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "schema_version")
@Setter
public class SchemaVersion {

  private Integer installedRank;
  private String version;
  private String description;
  private String type;
  private String script;
  private Integer checksum;
  private String installedBy;
  private Timestamp installedOn;
  private Integer executionTime;
  private boolean success;

  @Id
  @Column(name = "installed_rank")
  public Integer getInstalledRank() {
    return installedRank;
  }

  @Basic
  @Column(name = "version")
  public String getVersion() {
    return version;
  }

  @Basic
  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @Basic
  @Column(name = "type")
  public String getType() {
    return type;
  }

  @Basic
  @Column(name = "script")
  public String getScript() {
    return script;
  }

  @Basic
  @Column(name = "checksum")
  public Integer getChecksum() {
    return checksum;
  }

  @Basic
  @Column(name = "installed_by")
  public String getInstalledBy() {
    return installedBy;
  }

  @Basic
  @Column(name = "installed_on")
  public Timestamp getInstalledOn() {
    return installedOn;
  }

  @Basic
  @Column(name = "execution_time")
  public Integer getExecutionTime() {
    return executionTime;
  }

  @Basic
  @Column(name = "success")
  public boolean isSuccess() {
    return success;
  }
}
