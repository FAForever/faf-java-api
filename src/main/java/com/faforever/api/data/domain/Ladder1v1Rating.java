package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ladder1v1_rating")
@Include(rootLevel = true, type = "ladder1v1Rating")
public class Ladder1v1Rating extends Rating {
}
