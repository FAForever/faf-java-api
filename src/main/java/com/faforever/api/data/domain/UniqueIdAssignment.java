package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "unique_id_users")
@Include(name = UniqueIdAssignment.TYPE_NAME, rootLevel = false)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true)
public class UniqueIdAssignment extends AbstractEntity<UniqueIdAssignment> implements Serializable {
  public static final String TYPE_NAME = "uniqueIdAssignment";

  private Player user;
  private UniqueId uniqueId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  public Player getUser() {
    return user;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  // WTF? Who designed this table?
  @JoinColumn(name = "uniqueid_hash", referencedColumnName = "hash")
  public UniqueId getUniqueId() {
    return uniqueId;
  }
}
