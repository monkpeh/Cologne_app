package com.example.colognerecommendation.repository;

import com.example.colognerecommendation.model.Fragrance;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Fragrance} entities.
 *
 * <p>Extending {@link JpaRepository} gives this interface a full suite of database operations
 * for free — no SQL or implementation class is required:
 * <ul>
 *   <li>{@code findAll()} – retrieve every fragrance in the catalogue</li>
 *   <li>{@code findById(Integer)} – look up a single fragrance by its primary key</li>
 *   <li>{@code findAllById(Iterable)} – bulk lookup by a collection of IDs (used for user collections)</li>
 *   <li>{@code save(Fragrance)} – insert a new fragrance or update an existing one</li>
 *   <li>{@code deleteById(Integer)} – remove a fragrance by primary key</li>
 *   <li>{@code count()} – total number of fragrances (used to detect whether seeding is needed)</li>
 * </ul>
 *
 * <p>Spring generates a concrete implementation of this interface at application startup
 * using the entity metadata and the configured H2 data source.
 */
public interface FragranceRepository extends JpaRepository<Fragrance, Integer> {
}
