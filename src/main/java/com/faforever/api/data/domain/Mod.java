package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.ModChangeListener;
import com.faforever.api.security.elide.permission.AdminModCheck;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "\"mod\"")
@Include(name = Mod.TYPE_NAME)
@Data
@NoArgsConstructor
@EntityListeners(ModChangeListener.class)
public class Mod implements DefaultEntity, OwnableEntity {

  public static final String TYPE_NAME = "mod";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;


  @Column(name = "recommended")
  @NotNull
  @UpdatePermission(expression = AdminModCheck.EXPRESSION)
  private boolean recommended;

  @Column(name = "display_name")
  @Size(max = 100)
  @NotNull
  private String displayName;

  @Column(name = "author")
  @Size(max = 100)
  @NotNull
  private String author;

  @OneToMany(mappedBy = "mod", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  @Valid
  private List<ModVersion> versions;

  @ManyToOne
  @JoinColumnsOrFormulas({
    @JoinColumnOrFormula(
      formula = @JoinFormula(
        value = "(SELECT mod_version.id FROM mod_version WHERE mod_version.mod_id = id ORDER BY mod_version.version DESC LIMIT 1)",
        referencedColumnName = "id")
    )
  })
  private ModVersion latestVersion;

  @ManyToOne
  @JoinColumn(name = "uploader")
  private Player uploader;

  @OneToOne(mappedBy = "mod")
  @UpdatePermission(expression = Prefab.ALL)
  private ModReviewsSummary reviewsSummary;

  @Transient
  @Override
  public Login getEntityOwner() {
    return uploader;
  }
}
