package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "global_rating_rank_view")
@Include(name = "globalRating")
public class GlobalRating extends Rating {
}
