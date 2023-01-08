package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static com.faforever.api.data.domain.CoopScenario.TYPE_NAME;

@Entity
@Table(name = "coop_scenario")
@Include(name = TYPE_NAME)
@Setter
public class CoopScenario {
  public static final String TYPE_NAME = "coopScenario";

  private int id;
  private int order;
  private Type type;
  private Faction faction;
  private String name;
  private String description;
  private List<CoopMap> maps = new ArrayList<>();

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "order")
  public int getOrder() {
    return order;
  }

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  public Type getType() {
    return type;
  }

  @Column(name = "faction")
  @Enumerated(EnumType.STRING)
  public Faction getFaction() {
    return faction;
  }

  @Column(name = "name")
  public String getName() {
    return name;
  }

  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @OneToMany(mappedBy = "scenario", fetch = FetchType.LAZY)
  @BatchSize(size = 1000)
  public List<CoopMap> getMaps() {
    return maps;
  }

  private enum Type {
    SC, SCFA, CUSTOM
  }

  private enum Faction {
    UEF, CYBRAN, AEON, SERAPHIM, CUSTOM
  }
}
