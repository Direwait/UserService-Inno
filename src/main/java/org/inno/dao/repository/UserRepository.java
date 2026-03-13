package org.inno.dao.repository;

import jakarta.persistence.criteria.JoinType;
import org.inno.dao.model.UserModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID>, JpaSpecificationExecutor<UserModel> {

    @Query("SELECT u FROM UserModel u LEFT JOIN FETCH u.cards WHERE u.id = :userId")
    Optional<UserModel> findUserWithCardsById(@Param("userId") UUID userId);

    static Specification<UserModel> filterByNameOrSurname(String search) {
        return (root, query, cb) -> {
            assert query != null;
            if (Long.class != query.getResultType()) {
                root.fetch("cards", JoinType.LEFT);
            }
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("surname")), pattern)
            );
        };
    }

    boolean existsByEmail(String email);
}
