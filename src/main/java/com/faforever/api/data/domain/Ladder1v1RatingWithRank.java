package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ladder1v1_rating_rank_view")
@Include(rootLevel = true, type = "ladder1v1RatingWithRank")
public class Ladder1v1RatingWithRank extends RatingWithRank {
}
