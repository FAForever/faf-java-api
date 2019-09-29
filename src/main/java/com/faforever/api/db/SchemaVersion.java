package com.faforever.api.db;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "schema_version")
@Getter
@Setter
public class SchemaVersion {

  @Id
  @Column(name = "installed_rank")
  private Integer installedRank;

  @Basic
  @Column(name = "version")
  private String version;

  @Basic
  @Column(name = "description")
  private String description;

  @Basic
  @Column(name = "type")
  private String type;

  @Basic
  @Column(name = "script")
  private String script;

  @Basic
  @Column(name = "checksum")
  private Integer checksum;

  @Basic
  @Column(name = "installed_by")
  private String installedBy;

  @Basic
  @Column(name = "installed_on")
  private Timestamp installedOn;

  @Basic
  @Column(name = "execution_time")
  private Integer executionTime;

  @Basic
  @Column(name = "success")
  private boolean success;
}
