package com.example.subscriptions.repository;

import com.example.subscriptions.entity.Category;
import com.example.subscriptions.entity.Obligation;
import com.example.subscriptions.entity.Status;
import org.springframework.data.jpa.domain.Specification;

public class ObligationSpecifications {
    public static Specification<Obligation> hasCategory(Category category) {
        return (root, query, criteriaBuilder) ->
                category == null ? null : criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Obligation> hasStatus(Status status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }


}
