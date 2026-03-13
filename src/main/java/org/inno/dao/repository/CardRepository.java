package org.inno.dao.repository;

import org.inno.dao.model.CardModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<CardModel, UUID>, JpaSpecificationExecutor<CardModel> {

    int countByUserId(UUID userId);

    @Query("SELECT c FROM CardModel c JOIN FETCH c.user WHERE c.user.id = :userId")
    List<CardModel> findAllCardsByUserIdWithUser(@Param("userId") UUID userId);

    static Specification<CardModel> filterByNumber(String number) {
        return (root, query, cb) -> {
            if (number == null || number.isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + number.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("number")), pattern);
        };
    }

    List<CardModel> findAllByUser_Id(UUID id);
}
